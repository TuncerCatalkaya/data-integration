package org.dataintegration.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dataintegration.cache.DataIntegrationCache;
import org.dataintegration.exception.checked.ScopeHeaderValidationException;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.mapper.ScopeMapper;
import org.dataintegration.model.HeaderModel;
import org.dataintegration.model.ScopeModel;
import org.dataintegration.service.MappingsService;
import org.dataintegration.service.ProjectsService;
import org.dataintegration.service.ScopesService;
import org.dataintegration.usecase.api.ScopesMethods;
import org.dataintegration.usecase.model.CreateOrUpdateScopeHeadersRequestModel;
import org.mapstruct.factory.Mappers;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
class Scopes implements ScopesMethods {

    private final ScopeMapper scopeMapper = Mappers.getMapper(ScopeMapper.class);
    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final MappingsService mappingsService;
    private final DataIntegrationCache dataIntegrationCache;

    public ScopeModel createOrGetScope(UUID projectId, String scopeKey, boolean external, String createdBy) {
        return Optional.of(projectsService.getProject(projectId, createdBy))
                .map(projectEntity -> scopesService.createOrGetScope(projectEntity, scopeKey, external))
                .map(scopeMapper::scopeEntityToScope)
                .orElse(null);
    }

    @Override
    public LinkedHashSet<HeaderModel> createOrUpdateScopeHeaders(UUID projectId, UUID scopeId,
                                                                 CreateOrUpdateScopeHeadersRequestModel createOrUpdateScopeHeadersRequest,
                                                                 String createdBy) throws ScopeHeaderValidationException {
        projectsService.isPermitted(projectId, createdBy);
        scopesService.validateHeaders(createOrUpdateScopeHeadersRequest.getHeaders());
        scopesService.updateHeaders(scopeId, createOrUpdateScopeHeadersRequest.getHeaders());
        return scopesService.get(scopeId).getHeaders();
    }

    public void interruptScope(UUID projectId, UUID scopeId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        dataIntegrationCache.getInterruptingScopes().add(scopeId);
    }

    public LinkedHashSet<HeaderModel> getScopeHeaders(UUID projectId, UUID scopeId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        final ScopeEntity scopeEntity = scopesService.getAndCheckIfScopeFinished(scopeId);
        return scopeEntity.getHeaders();
    }

    public List<ScopeModel> getAllScopes(UUID projectId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        return scopesService.getAll(projectId).stream()
                .map(scopeMapper::scopeEntityToScope)
                .toList();
    }

    @Transactional
    public void markScopeForDeletion(UUID projectId, UUID scopeId, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        scopesService.markForDeletion(scopeId);
        mappingsService.markForDeletionByScope(scopeId);
    }

}
