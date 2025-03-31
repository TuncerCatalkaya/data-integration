package org.dataintegration.usecase;

import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.DatabaseEntity;
import org.dataintegration.jpa.entity.HostEntity;
import org.dataintegration.jpa.entity.MappedItemEntity;
import org.dataintegration.jpa.entity.MappingEntity;
import org.dataintegration.mapper.MappedItemMapper;
import org.dataintegration.model.DataIntegrationAPIModel;
import org.dataintegration.model.DataIntegrationHeaderAPIModel;
import org.dataintegration.model.DataIntegrationHeaderDataAPIModel;
import org.dataintegration.model.DataIntegrationInputAPIModel;
import org.dataintegration.model.DataIntegrationInputDataAPIModel;
import org.dataintegration.model.DataIntegrationResultAPIModel;
import org.dataintegration.model.ItemPropertiesModel;
import org.dataintegration.model.ItemStatusModel;
import org.dataintegration.model.MappedItemModel;
import org.dataintegration.service.MappedItemsService;
import org.dataintegration.service.MappingsService;
import org.dataintegration.service.ProjectsService;
import org.dataintegration.usecase.api.MappedItemsMethods;
import org.dataintegration.usecase.model.ApplyUnmappingRequestModel;
import org.dataintegration.usecase.model.IntegrateRequestModel;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
class MappedItems implements MappedItemsMethods {

    private final MappedItemMapper mappedItemMapper = Mappers.getMapper(MappedItemMapper.class);
    private final ProjectsService projectsService;
    private final MappingsService mappingsService;
    private final MappedItemsService mappedItemsService;
    private final HostsUsecase hostsUsecase;

    @SuppressWarnings("checkstyle:MethodLength")
    @Override
    public DataIntegrationAPIModel integrateMappedItems(UUID projectId, UUID mappingId, String language,
                                                        IntegrateRequestModel integrateRequest, String createdBy, String token) {
        projectsService.isPermitted(projectId, createdBy);
        final MappingEntity mapping = mappingsService.get(mappingId);
        final DatabaseEntity database = mapping.getDatabase();
        final HostEntity host = database.getHost();

        final Map<String, String[]> mappings = new HashMap<>();
        mapping.getMapping().forEach((source, targets) -> {
            for (String target : targets) {
                mappings.put(target, new String[]{source});
            }
        });

        final DataIntegrationHeaderAPIModel hostHeaders = hostsUsecase.getHostHeaders(host.getId(), language, token);
        final List<MappedItemEntity> mappedItems = mappedItemsService.getByMappedItemIds(integrateRequest.getMappedItemIds());

        final List<DataIntegrationInputDataAPIModel> inputs = new ArrayList<>();
        for (MappedItemEntity mappedItem : mappedItems) {
            final Map<String, String> data = new HashMap<>();
            for (DataIntegrationHeaderDataAPIModel target : hostHeaders.getHeaders()) {
                final String[] sources = mappings.get(target.getId());
                if (sources != null) {
                    for (String source : sources) {
                        final ItemPropertiesModel itemProperties;
                        if (mappedItem.getProperties() != null && mappedItem.getProperties().containsKey(target.getId())) {
                            itemProperties = mappedItem.getProperties().get(target.getId());
                        } else {
                            final ItemPropertiesModel itemPropertiesFromItem = mappedItem.getItem().getProperties().get(source);
                            if (itemPropertiesFromItem == null) {
                                itemProperties = ItemPropertiesModel.builder()
                                        .value("")
                                        .build();
                            } else {
                                itemProperties = itemPropertiesFromItem;
                            }
                        }
                        data.put(target.getId(), itemProperties.getValue());
                    }
                }
            }
            final DataIntegrationInputDataAPIModel dataIntegrationInputDataAPI = DataIntegrationInputDataAPIModel.builder()
                    .mappedItemId(mappedItem.getId())
                    .data(data)
                    .build();
            inputs.add(dataIntegrationInputDataAPI);
        }

        final DataIntegrationInputAPIModel dataIntegrationInputAPI = DataIntegrationInputAPIModel.builder()
                .inputs(inputs)
                .build();

        final WebClient webClient = WebClient.builder()
                .baseUrl(host.getBaseUrl())
                .build();
        final DataIntegrationAPIModel dataIntegrationAPIResponse = webClient.post()
                .uri(uriBuilder -> uriBuilder.path(host.getIntegrationPath() + "/{atlas}")
                        .queryParam("language", language)
                        .build(database.getName())
                )
                .headers(header -> header.setBearerAuth(token))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(dataIntegrationInputAPI))
                .retrieve()
                .onStatus(statusCode -> statusCode.is4xxClientError() || statusCode.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("Error in response")
                                .flatMap(body -> Mono.error(new ResponseStatusException(response.statusCode(), body)))
                )
                .bodyToMono(DataIntegrationAPIModel.class)
                .block();

        if (dataIntegrationAPIResponse != null) {
            for (DataIntegrationResultAPIModel dataIntegrationResultAPI : dataIntegrationAPIResponse.getIntegrationResults()) {
                final UUID mappedItemId = dataIntegrationResultAPI.getInput().getMappedItemId();
                final ItemStatusModel itemStatus =
                        dataIntegrationResultAPI.getErrorMessages().isEmpty() ? ItemStatusModel.INTEGRATED
                                : ItemStatusModel.FAILED;
                if (itemStatus == ItemStatusModel.INTEGRATED) {
                    final Map<String, String> data = dataIntegrationResultAPI.getInput().getData();
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        final String mappedKey = entry.getKey();
                        final String newValue = entry.getValue();
                        mappedItemsService.updateMappedItemProperty(mappedItemId, mappedKey, newValue);
                    }
                }
                mappedItemsService.updateMappedItemStatusByMappedItemId(mappedItemId, itemStatus);
                mappedItemsService.updateErrorMessagesByMappedItemId(mappedItemId,
                        dataIntegrationResultAPI.getErrorMessages());
            }
        }

        return dataIntegrationAPIResponse;
    }

    @Override
    public MappedItemModel updateMappedItemProperty(UUID projectId, UUID mappedItemId, String key, String newValue,
                                                    String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        final MappedItemEntity mappedItemEntity = mappedItemsService.updateMappedItemProperty(mappedItemId, key, newValue);
        return mappedItemMapper.mappedItemEntityToMappedItem(mappedItemEntity);
    }

    @Override
    public Page<MappedItemModel> getAllMappedItems(UUID projectId, UUID mappingId, boolean filterIntegratedItems,
                                                   String createdBy, Pageable pageable) {
        projectsService.isPermitted(projectId, createdBy);
        final Page<MappedItemEntity> mappedItemEntities =
                mappedItemsService.getByMapping(mappingId, filterIntegratedItems, pageable);
        final List<MappedItemModel> mappedItems = mappedItemEntities.stream()
                .map(mappedItemMapper::mappedItemEntityToMappedItem)
                .toList();
        return new PageImpl<>(mappedItems, mappedItemEntities.getPageable(), mappedItemEntities.getTotalElements());
    }

    @Override
    public void deleteMappedItems(UUID projectId, ApplyUnmappingRequestModel applyUnmappingRequest, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        mappedItemsService.deleteMappedItems(applyUnmappingRequest.getMappedItemIds());
    }

}
