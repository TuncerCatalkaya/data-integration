package org.dataintegration.usecase.api;

import org.dataintegration.model.DataIntegrationAPIModel;
import org.dataintegration.model.DataIntegrationInputAPIModel;
import org.dataintegration.model.MappedItemModel;
import org.dataintegration.usecase.model.ApplyUnmappingRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MappedItemsMethods {
    DataIntegrationAPIModel integrateMappedItems(UUID projectId, UUID mappingId, String language,
                                                 DataIntegrationInputAPIModel dataIntegrationInputAPI, String createdBy,
                                                 String token);

    MappedItemModel updateMappedItemProperty(UUID projectId, UUID mappedItemId, String key, String newValue, String createdBy);

    Page<MappedItemModel> getAllMappedItems(UUID projectId, UUID mappingId, String createdBy, Pageable pageable);

    void deleteMappedItems(UUID projectId, ApplyUnmappingRequestModel applyUnmappingRequest, String createdBy);
}
