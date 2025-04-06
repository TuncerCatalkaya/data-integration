package org.dataintegration.usecase.api;

import org.dataintegration.exception.checked.ScopeHeaderValidationException;
import org.dataintegration.model.HeaderModel;
import org.dataintegration.model.ScopeModel;
import org.dataintegration.usecase.model.CreateOrUpdateScopeHeadersRequestModel;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

public interface ScopesMethods {
    ScopeModel createOrGetScope(UUID projectId, String scopeKey, boolean external, String createdBy);

    LinkedHashSet<HeaderModel> createOrUpdateScopeHeaders(UUID projectId, UUID scopeId,
                                                          CreateOrUpdateScopeHeadersRequestModel createOrUpdateScopeHeadersRequest,
                                                          String createdBy) throws ScopeHeaderValidationException;

    void interruptScope(UUID projectId, UUID scopeId, String createdBy);

    LinkedHashSet<HeaderModel> getScopeHeaders(UUID projectId, UUID scopeId, String createdBy);

    List<ScopeModel> getAllScopes(UUID projectId, String createdBy);

    void markScopeForDeletion(UUID projectId, UUID scopeId, String createdBy);
}
