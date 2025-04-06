package org.dataintegration.usecase.api;

import org.dataintegration.model.ItemModel;
import org.dataintegration.usecase.model.UpdateItemPropertiesRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ItemsMethods {
    ItemModel updateItemProperty(UUID projectId, UUID itemId, String key, String newValue, String createdBy);

    void updateItemProperties(UUID projectId, UpdateItemPropertiesRequestModel updateItemPropertiesRequest, String key,
                              String newValue, String createdBy);

    Page<ItemModel> getAllItems(UUID projectId, UUID scopeId, UUID mappingId, String header, String search, String createdBy,
                                Pageable pageable);
}
