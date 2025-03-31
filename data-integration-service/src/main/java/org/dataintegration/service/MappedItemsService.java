package org.dataintegration.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.MappedItemFrozenException;
import org.dataintegration.exception.runtime.MappedItemNotFoundException;
import org.dataintegration.jpa.entity.MappedItemEntity;
import org.dataintegration.jpa.entity.MappingEntity;
import org.dataintegration.jpa.repository.JpaMappedItemRepository;
import org.dataintegration.model.ItemPropertiesModel;
import org.dataintegration.model.ItemStatusModel;
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
    public Map<UUID, List<UUID>> getItemsWithMappings(List<UUID> itemIds) {
        final Map<UUID, List<UUID>> itemToMappingsMap = new HashMap<>();
        itemIds.forEach(itemId -> {
            final List<MappedItemEntity> mappedItemEntities = jpaMappedItemRepository.findAllByItem_Id(itemId);
            itemToMappingsMap.put(itemId, mappedItemEntities.stream()
                    .map(mappedItemEntity -> mappedItemEntity.getMapping().getId())
                    .toList());
        });
        return itemToMappingsMap;
    }

    public Page<MappedItemEntity> getByMapping(UUID mappingId, boolean filterIntegratedItems, Pageable pageable) {
        final Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "item.lineNumber")));
        if (filterIntegratedItems) {
            return jpaMappedItemRepository.findAllByMappingIdAndStatusNot(mappingId, ItemStatusModel.INTEGRATED, pageable);
        } else {
            return jpaMappedItemRepository.findAllByMapping_Id(mappingId, pageRequest);
        }
    }

    public List<MappedItemEntity> getByMappedItemIds(List<UUID> mappedItemIds) {
        return jpaMappedItemRepository.findAllById(mappedItemIds);
    }

    public void updateMappedItemStatusByMappedItemId(UUID mappedItemId, ItemStatusModel itemStatus) {
        jpaMappedItemRepository.updateStatusById(mappedItemId, itemStatus);
    }

    public void updateErrorMessagesByMappedItemId(UUID mappedItemId, List<String> errorMessages) {
        jpaMappedItemRepository.updateErrorMessagesById(mappedItemId, errorMessages);
    }

    public MappedItemEntity updateMappedItemProperty(UUID mappedItemId, String mappedKey, String newValue) {
        final MappedItemEntity mappedItemEntity = getMappedItem(mappedItemId);
        if (mappedItemEntity.getStatus() == ItemStatusModel.INTEGRATED) {
            throw new MappedItemFrozenException("Mapped item with id " + mappedItemId + " is frozen (readonly).");
        }
        final Map<String, ItemPropertiesModel> mappedItemProperties =
                createOrGetProperties(mappedItemEntity, mappedKey, newValue);
        final ItemPropertiesModel itemProperties = newValue != null ? mappedItemProperties.get(mappedKey).toBuilder()
                .value(newValue)
                .build() : null;
        mappedItemProperties.put(mappedKey, itemProperties);
        mappedItemEntity.setProperties(mappedItemProperties);
        return jpaMappedItemRepository.save(mappedItemEntity);
    }

    private Map<String, ItemPropertiesModel> createOrGetProperties(MappedItemEntity mappedItemEntity, String mappedKey,
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

    private MappedItemEntity getMappedItem(UUID mappedItemId) {
        return jpaMappedItemRepository.findById(mappedItemId)
                .orElseThrow(() -> new MappedItemNotFoundException("Mapped item with id " + mappedItemId + " not found."));
    }

    public void deleteMappedItems(List<UUID> mappedItemIds) {
        jpaMappedItemRepository.deleteAllById(mappedItemIds);
    }

}
