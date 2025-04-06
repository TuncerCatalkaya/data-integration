package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.checked.ScopeHeaderValidationException;
import org.dataintegration.exception.runtime.ScopeNotFinishedException;
import org.dataintegration.exception.runtime.ScopeNotFoundException;
import org.dataintegration.jpa.entity.ProjectEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.jpa.repository.JpaScopeRepository;
import org.dataintegration.model.HeaderModel;
import org.dataintegration.model.cache.DataIntegrationCache;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for scopes.
 */
@Service
@RequiredArgsConstructor
public class ScopesService {

    private static final String SCOPE_WITH_ID = "Scope with id ";

    private final JpaScopeRepository jpaScopeRepository;
    private final DataIntegrationCache dataIntegrationCache;

    /**
     * Create or get scope by scope key through project.
     *
     * @param projectEntity project entity
     * @param scopeKey scope key
     * @param external is external (s3)
     * @return created or retrieved {@link ScopeEntity}
     */
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

    /**
     * Get scope and check if it is finished.
     *
     * @param scopeId scope id
     * @return {@link ScopeEntity}
     * @throws ScopeNotFinishedException in case scope is not finished with import yet
     */
    public ScopeEntity getAndCheckIfScopeFinished(UUID scopeId) {
        final ScopeEntity scopeEntity = get(scopeId);
        if (!scopeEntity.isFinished()) {
            throw new ScopeNotFinishedException(SCOPE_WITH_ID + scopeEntity.getId() + " is not finished with import process.");
        }
        return scopeEntity;
    }

    /**
     * Get scope by scope id.
     *
     * @param scopeId scope id
     * @return {@link ScopeEntity}
     * @throws ScopeNotFoundException in case scope entity is not found in database
     */
    public ScopeEntity get(UUID scopeId) {
        return jpaScopeRepository.findByIdAndDeleteFalse(scopeId)
                .orElseThrow(() -> new ScopeNotFoundException(SCOPE_WITH_ID + scopeId + " not found."));
    }

    /**
     * Get scope by project id and scope key.
     *
     * @param projectId project id
     * @param scopeKey scope key
     * @return {@link Optional} of {@link ScopeEntity}
     */
    public Optional<ScopeEntity> get(UUID projectId, String scopeKey) {
        return jpaScopeRepository.findByProject_IdAndKeyAndDeleteFalse(projectId, scopeKey);
    }

    /**
     * Get all scopes by project id.
     *
     * @param projectId project id
     * @return {@link List} of {@link ScopeEntity}
     */
    public List<ScopeEntity> getAll(UUID projectId) {
        return jpaScopeRepository.findAllByProject_idAndDeleteFalse(projectId, Sort.by(Sort.Direction.ASC, "createdDate"));
    }

    /**
     * Finish a scope (set flag).
     *
     * @param scopeId scope id
     */
    public void finish(UUID scopeId) {
        jpaScopeRepository.finish(scopeId);
    }

    /**
     * Update scope headers.
     *
     * @param scopeId scope id
     * @param headers headers that will be used to update
     * @return updated {@link LinkedHashSet} of {@link HeaderModel}
     */
    public LinkedHashSet<HeaderModel> updateHeaders(UUID scopeId, Set<HeaderModel> headers) {
        final ScopeEntity scope = get(scopeId);
        final Set<HeaderModel> updatedHeaders = new LinkedHashSet<>(scope.getHeaders() == null ? Collections.emptyList() : scope.getHeaders());

        final Map<String, HeaderModel> existingHeadersMap = updatedHeaders.stream()
                .collect(Collectors.toMap(HeaderModel::getId, header -> header));

        for (HeaderModel header : headers) {
            if (existingHeadersMap.containsKey(header.getId())) {
                HeaderModel existingHeader = existingHeadersMap.get(header.getId());
                existingHeader.setDisplay(header.getDisplay());
                existingHeader.setHidden(header.isHidden());
            } else {
                final HeaderModel newHeader = new HeaderModel(header.getId());
                updatedHeaders.add(newHeader);
            }
        }

        final Set<HeaderModel> sortedUpdatedHeaders = updatedHeaders.stream()
                .sorted(Comparator.comparingInt(header -> {
                    int index = 0;
                    for (HeaderModel referenceHeader : headers) {
                        if (referenceHeader.getId().equals(header.getId())) {
                            return index;
                        }
                        index++;
                    }
                    return Integer.MAX_VALUE;
                }))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        jpaScopeRepository.updateHeaders(scopeId, sortedUpdatedHeaders);
        return get(scopeId).getHeaders();
    }

    /**
     * Validate scope headers.
     *
     * @param headers headers that will be validated
     * @throws ScopeHeaderValidationException in case a validation error occurs
     */
    public void validateHeaders(Set<HeaderModel> headers) throws ScopeHeaderValidationException {
        if (CollectionUtils.isEmpty(headers)) {
            throw new ScopeHeaderValidationException("No scope headers provided, headers are empty.");
        }

        for (HeaderModel header : headers) {
            if (!StringUtils.hasText(header.getId())) {
                throw new ScopeHeaderValidationException("Header '" + header.getId() + "' has no text.");
            }
        }
    }

    /**
     * Mark scope for deletion by scope id.
     *
     * @param scopeId scope id
     */
    public void markForDeletion(UUID scopeId) {
        jpaScopeRepository.markForDeletion(scopeId);
        dataIntegrationCache.getMarkedForDeletionScopes().add(scopeId);
    }

    /**
     * Mark all scopes for deletion by project id.
     *
     * @param projectId project id
     * @return {@link List} of marked for deletion {@link ScopeEntity}
     */
    public List<ScopeEntity> markForDeletionByProjectId(UUID projectId) {
        final List<ScopeEntity> scopeEntities = jpaScopeRepository.findAllByProject_idAndDeleteFalse(projectId, Sort.unsorted());
        scopeEntities.forEach(scopeEntity -> scopeEntity.setDelete(true));
        return jpaScopeRepository.saveAll(scopeEntities);
    }

}
