package org.dataintegration.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.MappedItemNotFoundException;
import org.dataintegration.jpa.entity.MappedItemEntity;
import org.dataintegration.jpa.entity.MappingEntity;
import org.dataintegration.jpa.repository.JpaMappedItemRepository;
import org.dataintegration.model.ItemPropertiesModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MappedItemsService {

    private final JpaMappedItemRepository jpaMappedItemRepository;

    public void applyMapping(List<MappedItemEntity> mappedItemEntities) {
        jpaMappedItemRepository.saveAll(mappedItemEntities);
    }

    @Transactional
    public Map<UUID, List<UUID>> getItemWithMappings(List<UUID> itemIds) {
        final Map<UUID, List<UUID>> itemToMappingsMap = new HashMap<>();
        itemIds.forEach(itemId -> {
            final List<MappedItemEntity> mappedItemEntities = jpaMappedItemRepository.findAllByItem_Id(itemId);
            itemToMappingsMap.put(itemId, mappedItemEntities.stream()
                    .map(mappedItemEntity -> mappedItemEntity.getMapping().getId())
                    .toList());
        });
        return itemToMappingsMap;
    }

    public Page<MappedItemEntity> getByMapping(UUID mappingId, Pageable pageable) {
        final Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "item.lineNumber")));
        return jpaMappedItemRepository.findAllByMapping_Id(mappingId, pageRequest);
    }

    public MappedItemEntity updateMappedItemProperty(UUID mappedItemId, String mappedKey, String newValue) {
        final MappedItemEntity mappedItemEntity = getMappedItem(mappedItemId);
        final Map<String, ItemPropertiesModel> mappedItemProperties =
                createOrGetProperties(mappedItemEntity, mappedKey, newValue);
        final String originalValueInDatabase = Optional.ofNullable(mappedItemProperties.get(mappedKey).getOriginalValue())
                .orElseGet(() -> mappedItemProperties.get(mappedKey).getValue());
        final boolean edited = !originalValueInDatabase.equals(newValue);
        mappedItemProperties.put(mappedKey, mappedItemProperties.get(mappedKey).toBuilder()
                .value(newValue)
                .originalValue(edited ? originalValueInDatabase : null)
                .build());
        mappedItemEntity.setProperties(mappedItemProperties);
        return jpaMappedItemRepository.save(mappedItemEntity);
    }

    private Map<String, ItemPropertiesModel> createOrGetProperties(MappedItemEntity mappedItemEntity, String mappedKey, String newValue) {
        final Map<String, ItemPropertiesModel> mappedItemProperties = Optional.ofNullable(mappedItemEntity.getProperties())
                .orElse(new HashMap<>());
        final MappingEntity mappingEntity = mappedItemEntity.getMapping();
        for (Map.Entry<String, String[]> mappingEntry : mappingEntity.getMapping().entrySet()) {
            for (String value : mappingEntry.getValue()) {
                if (value.equals(mappedKey)) {
                    final ItemPropertiesModel copiedItemProperties =
                            mappedItemEntity.getItem().getProperties().get(mappingEntry.getKey());
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

    private MappedItemEntity getMappedItem(UUID mappedItemId) {
        return jpaMappedItemRepository.findById(mappedItemId)
                .orElseThrow(() -> new MappedItemNotFoundException("Mapped item with id " + mappedItemId + " not found."));
    }

    public void deleteMappedItems(List<UUID> mappedItemIds) {
        jpaMappedItemRepository.deleteAllById(mappedItemIds);
    }

}
