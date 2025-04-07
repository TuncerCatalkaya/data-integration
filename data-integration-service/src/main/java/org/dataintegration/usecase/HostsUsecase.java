package org.dataintegration.usecase;

import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.HostEntity;
import org.dataintegration.mapper.CreateOrUpdateHostsMapper;
import org.dataintegration.mapper.HostMapper;
import org.dataintegration.model.DataIntegrationHeaderAPIModel;
import org.dataintegration.model.HostModel;
import org.dataintegration.service.HostsService;
import org.dataintegration.usecase.model.CreateOrUpdateHostsRequestModel;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Usecase for hosts.
 */
@Component
@RequiredArgsConstructor
public class HostsUsecase {

    private final CreateOrUpdateHostsMapper createOrUpdateHostsMapper =
            Mappers.getMapper(CreateOrUpdateHostsMapper.class);
    private final HostMapper hostMapper = Mappers.getMapper(HostMapper.class);
    private final HostsService hostsService;

    /**
     * Create or update host.
     *
     * @param createOrUpdateHostsRequest {@link CreateOrUpdateHostsRequestModel}
     * @return created or updated {@link HostModel}
     */
    public HostModel createOrUpdateHost(CreateOrUpdateHostsRequestModel createOrUpdateHostsRequest) {
        final HostEntity hostEntity = createOrUpdateHostsMapper.createOrUpdateHostsToHostEntity(createOrUpdateHostsRequest);
        return Optional.of(hostEntity)
                .map(hostsService::createOrUpdateHost)
                .map(hostMapper::hostEntityToHost)
                .orElse(null);
    }

    /**
     * Get all hosts.
     *
     * @return {@link Set} of {@link HostModel}
     */
    public Set<HostModel> getAllHosts() {
        return hostsService.getAllHosts().stream()
                .map(hostMapper::hostEntityToHost)
                .collect(Collectors.toSet());
    }

    /**
     * Get host headers.
     *
     * @param hostId host id
     * @param language language
     * @param token token
     * @return {@link DataIntegrationHeaderAPIModel}
     */
    public DataIntegrationHeaderAPIModel getHostHeaders(UUID hostId, String language, String token) {
        final HostEntity host = hostsService.getHost(hostId);
        final WebClient webClient = WebClient.builder()
                .baseUrl(host.getBaseUrl())
                .build();
        DataIntegrationHeaderAPIModel dataIntegrationHeader = webClient.get()
                .uri(uriBuilder -> uriBuilder.path(host.getHeaderPath())
                        .queryParam("language", language)
                        .build()
                )
                .headers(header -> header.setBearerAuth(token))
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(body))))
                .bodyToMono(DataIntegrationHeaderAPIModel.class)
                .onErrorResume(e -> Mono.empty())
                .block();

        if (dataIntegrationHeader != null) {
            hostsService.updateHeadersByHostId(hostId,  dataIntegrationHeader.getHeaders());
            return dataIntegrationHeader;
        }

        return DataIntegrationHeaderAPIModel.builder()
                .headers(host.getHeaders())
                .build();
    }

    /**
     * Delete host by host id.
     *
     * @param hostId host id
     */
    public void deleteHost(UUID hostId) {
        hostsService.deleteHost(hostId);
    }

}
