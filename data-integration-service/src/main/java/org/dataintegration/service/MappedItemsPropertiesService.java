package org.dataintegration.service;

import org.dataintegration.jpa.entity.MappedItemEntity;
import org.dataintegration.jpa.entity.MappingEntity;
import org.dataintegration.model.ItemPropertiesModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for mapped item properties.
 */
@Service
public class MappedItemsPropertiesService {

    /**
     * Create or get properties.
     *
     * @param mappedItemEntity {@link MappedItemEntity}
     * @param mappedKey        mapped key name
     * @param newValue         new value
     * @return {@link Map} where key {@link String} is the mappedKey, and value {@link ItemPropertiesModel} is either
     * the created property, or if already there then the retrieved.
     */
    public Map<String, ItemPropertiesModel> createOrGetProperties(MappedItemEntity mappedItemEntity, String mappedKey,
                                                                  String newValue) {
        final Map<String, ItemPropertiesModel> mappedItemProperties = Optional.ofNullable(mappedItemEntity.getProperties())
                .orElse(new HashMap<>());
        final MappingEntity mappingEntity = mappedItemEntity.getMapping();
        for (Map.Entry<String, String[]> mappingEntry : mappingEntity.getMapping().entrySet()) {
            for (String value : mappingEntry.getValue()) {
                if (value.equals(mappedKey)) {
                    final ItemPropertiesModel copiedItemProperties =
                            Optional.ofNullable(mappedItemEntity.getItem().getProperties().get(mappingEntry.getKey()))
                                    .orElse(ItemPropertiesModel.builder()
                                            .value("")
                                            .build());
                    mappedItemProperties.putIfAbsent(value, ItemPropertiesModel.builder()
                            .value(newValue)
                            .originalValue(copiedItemProperties.getValue())
                            .build());
                    return mappedItemProperties;
                }
            }
        }
        return mappedItemProperties;
    }

}
