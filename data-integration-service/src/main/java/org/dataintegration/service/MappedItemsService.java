package org.dataintegration.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.MappedItemFrozenException;
import org.dataintegration.exception.runtime.MappedItemNotFoundException;
import org.dataintegration.jpa.entity.MappedItemEntity;
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
import java.util.UUID;

/**
 * Service for mapped items.
 */
@Service
@RequiredArgsConstructor
public class MappedItemsService {

    private final JpaMappedItemRepository jpaMappedItemRepository;
    private final MappedItemsPropertiesService mappedItemsPropertiesService;

    /**
     * Apply mapped items by saving them.
     *
     * @param mappedItemEntities {@link List} of {@link MappedItemEntity}
     */
    public void applyMapping(List<MappedItemEntity> mappedItemEntities) {
        jpaMappedItemRepository.saveAll(mappedItemEntities);
    }

    /**
     * Get items with mappings.
     *
     * @param itemIds item ids
     * @return {@link Map} where key is equal to item ids and value is {@link List} of mapped item ids
     */
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

    /**
     * Get all mapped items paginated and filtered by mapping id.
     *
     * @param mappingId             mapping id
     * @param filterIntegratedItems filter already integrated items (true = filtered out)
     * @param pageable              {@link Pageable}
     * @return {@link Page} of {@link MappedItemEntity}
     */
    public Page<MappedItemEntity> getByMapping(UUID mappingId, boolean filterIntegratedItems, Pageable pageable) {
        final Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "item.lineNumber")));
        if (filterIntegratedItems) {
            return jpaMappedItemRepository.findAllByMapping_IdAndStatusNot(mappingId, ItemStatusModel.INTEGRATED, pageRequest);
        } else {
            return jpaMappedItemRepository.findAllByMapping_Id(mappingId, pageRequest);
        }
    }

    /**
     * Get by mapped item ids.
     *
     * @param mappedItemIds {@link List} of mapped item ids
     * @return {@link List} of {@link MappedItemEntity}
     */
    public List<MappedItemEntity> getByMappedItemIds(List<UUID> mappedItemIds) {
        return jpaMappedItemRepository.findAllById(mappedItemIds);
    }


    /**
     * Update mapped item status by mapped item id.
     *
     * @param mappedItemId mapped item id
     * @param itemStatus   {@link ItemStatusModel}
     */
    public void updateMappedItemStatusByMappedItemId(UUID mappedItemId, ItemStatusModel itemStatus) {
        jpaMappedItemRepository.updateStatusById(mappedItemId, itemStatus);
    }

    /**
     * Update error messages by mapped item id.
     *
     * @param mappedItemId  mapped item id
     * @param errorMessages {@link List} of error messages
     */
    public void updateErrorMessagesByMappedItemId(UUID mappedItemId, List<String> errorMessages) {
        jpaMappedItemRepository.updateErrorMessagesById(mappedItemId, errorMessages);
    }

    /**
     * Get mapped item by mapped item id.
     *
     * @param mappedItemId mapped item id
     * @return {@link MappedItemEntity}
     * @throws MappedItemNotFoundException in case mapped item entity is not found in database
     */
    public MappedItemEntity getMappedItem(UUID mappedItemId) throws MappedItemNotFoundException {
        return jpaMappedItemRepository.findById(mappedItemId)
                .orElseThrow(() -> new MappedItemNotFoundException("Mapped item with id " + mappedItemId + " not found."));
    }

    /**
     * Update mapped item property
     *
     * @param mappedItemId mapped item id
     * @param mappedKey    mapped key name
     * @param newValue     new value
     * @return updated {@link MappedItemEntity}
     * @throws MappedItemFrozenException in case the mapped item is integrated, the data can not be updated
     */
    public MappedItemEntity updateMappedItemProperty(UUID mappedItemId, String mappedKey, String newValue) {
        final MappedItemEntity mappedItemEntity = getMappedItem(mappedItemId);
        if (mappedItemEntity.getStatus() == ItemStatusModel.INTEGRATED) {
            throw new MappedItemFrozenException("Mapped item with id " + mappedItemId + " is frozen (readonly).");
        }
        final Map<String, ItemPropertiesModel> mappedItemProperties =
                mappedItemsPropertiesService.createOrGetProperties(mappedItemEntity, mappedKey, newValue);
        final ItemPropertiesModel itemProperties = newValue != null ? mappedItemProperties.get(mappedKey).toBuilder()
                .value(newValue)
                .build() : null;
        mappedItemProperties.put(mappedKey, itemProperties);
        mappedItemEntity.setProperties(mappedItemProperties);
        return jpaMappedItemRepository.save(mappedItemEntity);
    }

    /**
     * Delete mapped items by mapped item ids.
     *
     * @param mappedItemIds {@link List} of mapped item ids
     */
    public void deleteMappedItems(List<UUID> mappedItemIds) {
        jpaMappedItemRepository.deleteAllById(mappedItemIds);
    }

}
