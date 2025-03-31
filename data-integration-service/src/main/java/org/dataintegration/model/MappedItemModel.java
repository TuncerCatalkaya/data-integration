package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappedItemModel {

    private UUID id;
    private Map<String, ItemPropertiesModel> properties;
    private ItemStatusModel status;
    private List<String> errorMessages;

    private ItemModel item;

}
