package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.cache.DataIntegrationCache;
import org.dataintegration.exception.checked.ScopeHeaderValidationException;
import org.dataintegration.exception.runtime.ScopeNotFinishedException;
import org.dataintegration.exception.runtime.ScopeNotFoundException;
import org.dataintegration.jpa.entity.ProjectEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.jpa.repository.JpaScopeRepository;
import org.dataintegration.model.HeaderModel;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScopesService {

    private static final String SCOPE_WITH_ID = "Scope with id ";

    private final JpaScopeRepository jpaScopeRepository;
    private final DataIntegrationCache dataIntegrationCache;

    public ScopeEntity createOrGetScope(ProjectEntity projectEntity, String scopeKey, boolean external) {
        return get(projectEntity.getId(), scopeKey)
                .orElseGet(() -> {
                    final ScopeEntity scopeEntity = new ScopeEntity();
                    scopeEntity.setKey(scopeKey);
                    scopeEntity.setExternal(external);
                    scopeEntity.setFinished(false);
                    scopeEntity.setDelete(false);
                    scopeEntity.setProject(projectEntity);
                    return jpaScopeRepository.save(scopeEntity);
                });
    }

    public ScopeEntity getAndCheckIfScopeFinished(UUID scopeId) {
        final ScopeEntity scopeEntity = jpaScopeRepository.findByIdAndDeleteFalse(scopeId)
                .orElseThrow(() -> new ScopeNotFoundException(SCOPE_WITH_ID + scopeId + " not found."));
        if (!scopeEntity.isFinished()) {
            throw new ScopeNotFinishedException(SCOPE_WITH_ID + scopeEntity.getId() + " is not finished with import process.");
        }
        return scopeEntity;
    }

    public ScopeEntity get(UUID scopeId) {
        return jpaScopeRepository.findByIdAndDeleteFalse(scopeId)
                .orElseThrow(() -> new ScopeNotFoundException(SCOPE_WITH_ID + scopeId + " not found."));
    }

    public Optional<ScopeEntity> get(UUID projectId, String scopeKey) {
        return jpaScopeRepository.findByProject_IdAndKeyAndDeleteFalse(projectId, scopeKey);
    }

    public List<ScopeEntity> getAll(UUID projectId) {
        return jpaScopeRepository.findAllByProject_idAndDeleteFalse(projectId, Sort.by(Sort.Direction.ASC, "createdDate"));
    }

    public void finish(UUID scopeId) {
        jpaScopeRepository.finish(scopeId);
    }

    public void updateHeaders(UUID scopeId, Set<HeaderModel> headers) {
        jpaScopeRepository.updateHeaders(scopeId, headers);
    }

    public void validateHeaders(Set<HeaderModel> headers) throws ScopeHeaderValidationException {
        if (CollectionUtils.isEmpty(headers)) {
            throw new ScopeHeaderValidationException("No scope headers provided, headers are empty.");
        }

        for (HeaderModel header : headers) {
            if (!StringUtils.hasText(header.getName())) {
                throw new ScopeHeaderValidationException("Header '" + header.getName() + "' has no text.");
            }
        }
    }

    public void markForDeletion(UUID scopeId) {
        jpaScopeRepository.markForDeletion(scopeId);
        dataIntegrationCache.getMarkedForDeletionScopes().add(scopeId);
    }

    public List<ScopeEntity> markForDeletionByProjectId(UUID projectId) {
        final List<ScopeEntity> scopeEntities = jpaScopeRepository.findAllByProject_idAndDeleteFalse(projectId, Sort.unsorted());
        scopeEntities.forEach(scopeEntity -> scopeEntity.setDelete(true));
        return jpaScopeRepository.saveAll(scopeEntities);
    }

}
