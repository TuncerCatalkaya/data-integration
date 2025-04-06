package org.dataintegration.usecase.api;

import org.dataintegration.model.MappingModel;
import org.dataintegration.usecase.model.ApplyMappingRequestModel;
import org.dataintegration.usecase.model.CreateOrUpdateMappingsRequestModel;

import java.util.List;
import java.util.UUID;

public interface MappingsMethods {
    MappingModel createOrUpdateMapping(UUID projectId, UUID scopeId, CreateOrUpdateMappingsRequestModel createMappingsRequest,
                                       String createdBy, String token);

    void applyMapping(UUID projectId, ApplyMappingRequestModel applyMappingRequest, String createdBy);

    List<MappingModel> getAllMappings(UUID projectId, UUID scopeId, String createdBy);

    void markMappingForDeletion(UUID projectId, UUID mappingId, String createdBy);
}
