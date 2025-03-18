package org.dataintegration.usecase;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.MappingValidationException;
import org.dataintegration.jpa.entity.DatabaseEntity;
import org.dataintegration.jpa.entity.ItemEntity;
import org.dataintegration.jpa.entity.MappedItemEntity;
import org.dataintegration.jpa.entity.MappingEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.mapper.MappingMapper;
import org.dataintegration.model.HeaderModel;
import org.dataintegration.model.ItemStatusModel;
import org.dataintegration.model.MappingModel;
import org.dataintegration.service.HostsService;
import org.dataintegration.service.ItemsService;
import org.dataintegration.service.MappedItemsService;
import org.dataintegration.service.MappingsService;
import org.dataintegration.service.ProjectsService;
import org.dataintegration.service.ScopesService;
import org.dataintegration.usecase.api.MappingsMethods;
import org.dataintegration.usecase.model.ApplyMappingRequestModel;
import org.dataintegration.usecase.model.CreateOrUpdateMappingsRequestModel;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class Mappings implements MappingsMethods {

    private final MappingMapper mappingMapper = Mappers.getMapper(MappingMapper.class);
    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final ItemsService itemsService;
    private final MappingsService mappingsService;
    private final MappedItemsService mappedItemsService;
    private final HostsService hostsService;

    public MappingModel createOrUpdateMapping(UUID projectId, UUID scopeId,
                                              CreateOrUpdateMappingsRequestModel createOrUpdateMappingsRequest,
                                              String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        final ScopeEntity scopeEntity = scopesService.getAndCheckIfScopeFinished(scopeId);
        final UUID mappingId = createOrUpdateMappingsRequest.getMappingId();
        final Map<String, String[]> mapping = createOrUpdateMappingsRequest.getMapping();
        final LinkedHashSet<HeaderModel> headers = scopeEntity.getHeaders().stream()
                .filter(Predicate.not(HeaderModel::isHidden))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        mappingsService.validateMapping(mappingId, mapping, headers);
        final DatabaseEntity databaseEntity = hostsService.getDatabase(createOrUpdateMappingsRequest.getDatabaseId());
        final MappingEntity mappingEntity = (mappingId != null) ? mappingsService.get(mappingId) : getNewMappingEntity();
        mappingEntity.setId(mappingId);
        mappingEntity.setName(createOrUpdateMappingsRequest.getMappingName());
        mappingEntity.setMapping(mapping);
        mappingEntity.setDatabase(databaseEntity);
        mappingEntity.setScope(scopeEntity);
        return Optional.of(mappingEntity)
                .map(mappingsService::createOrUpdateMapping)
                .map(mappingMapper::mappingEntityToMapping)
                .orElse(null);
    }

    public void applyMapping(UUID projectId, ApplyMappingRequestModel applyMappingRequest, String createdBy) {
        try {
            projectsService.isPermitted(projectId, createdBy);
            final MappingEntity mappingEntity = mappingsService.get(applyMappingRequest.getMappingId());
            final List<ItemEntity> itemEntities = itemsService.getAll(applyMappingRequest.getItemIds());

            final UUID scopeId = mappingEntity.getScope().getId();
            scopesService.getAndCheckIfScopeFinished(scopeId);
            final boolean scopeIdDoesNotMatch = itemEntities.stream()
                    .anyMatch(itemEntity -> !itemEntity.getScope().getId().equals(scopeId));
            if (scopeIdDoesNotMatch) {
                throw new MappingValidationException(
                        "Items are not valid, because at least one of the items has a different scope than the scope of the specified mapping.");
            }

            final List<MappedItemEntity> mappedItemEntities = itemEntities.stream()
                    .map(itemEntity -> {
                        final MappedItemEntity mappedItemEntity = new MappedItemEntity();
                        mappedItemEntity.setMapping(mappingEntity);
                        mappedItemEntity.setItem(itemEntity);
                        mappedItemEntity.setStatus(ItemStatusModel.MAPPED);
                        return mappedItemEntity;
                    })
                    .toList();
            mappedItemsService.applyMapping(mappedItemEntities);
        } catch (DataIntegrityViolationException ex) {
            throw new MappingValidationException(ex);
        }
    }

    public List<MappingModel> getAllMappings(UUID projectId, UUID scopeId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        return mappingsService.getAll(scopeId).stream()
                .map(mappingMapper::mappingEntityToMapping)
                .toList();
    }

    public void markMappingForDeletion(UUID projectId, UUID mappingId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        mappingsService.markForDeletion(mappingId);
    }

    private MappingEntity getNewMappingEntity() {
        final MappingEntity mappingEntity = new MappingEntity();
        mappingEntity.setFinished(false);
        mappingEntity.setProcessing(false);
        mappingEntity.setDelete(false);
        mappingEntity.setLastProcessedBatch(-1);
        return mappingEntity;
    }

}
