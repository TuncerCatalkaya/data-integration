package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.ItemNotFoundException;
import org.dataintegration.jpa.entity.ItemEntity;
import org.dataintegration.jpa.repository.JpaItemRepository;
import org.dataintegration.model.ItemPropertiesModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for items.
 */
@Service
@RequiredArgsConstructor
public class ItemsService {

    private final JpaItemRepository jpaItemRepository;

    /**
     * Get all items paginated and filtered.
     *
     * @param scopeId scope id, so the items fetched are only from that scope
     * @param mappingId mapping id that will be used to filter already mapped items
     * @param searchHeader the header that is searched for (if null or empty then it is a free text search across all headers)
     * @param searchText text that is searched for
     * @param pageable {@link Pageable}
     * @return {@link Page} of {@link ItemEntity}
     */
    public Page<ItemEntity> getAll(UUID scopeId, UUID mappingId, String searchHeader, String searchText, Pageable pageable) {
        final Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "line_number")));
        if (mappingId != null) {
            if (StringUtils.hasLength(searchText)) {
                if (StringUtils.hasText(searchHeader)) {
                    return jpaItemRepository.findAllByScopeIdAndMappingIdNotInMappedItemsAndDynamicHeader(scopeId, mappingId,
                            searchHeader, searchText, pageRequest);
                } else {
                    return jpaItemRepository.findAllByScopeIdAndMappingIdNotInMappedItemsWithFreeTextSearch(scopeId, mappingId,
                            searchText, pageRequest);
                }
            } else {
                return jpaItemRepository.findAllByScopeIdAndMappingIdNotInMappedItems(scopeId, mappingId, pageRequest);
            }
        } else {
            if (StringUtils.hasLength(searchText)) {
                if (StringUtils.hasText(searchHeader)) {
                    return jpaItemRepository.findAllByScopeIdAndDynamicHeader(scopeId, searchHeader, searchText, pageRequest);
                } else {
                    return jpaItemRepository.findAllByScopeIdWithFreeTextSearch(scopeId, searchText, pageRequest);
                }
            } else {
                return jpaItemRepository.findAllByScopeId(scopeId, pageRequest);
            }
        }
    }

    /**
     * Get all items by item ids.
     *
     * @param itemIds list of item ids
     * @return {@link List} of {@link ItemEntity}
     */
    public List<ItemEntity> getAll(List<UUID> itemIds) {
        return jpaItemRepository.findAllById(itemIds);
    }

    /**
     * Get item by item id.
     *
     * @param itemId item id
     * @return {@link ItemEntity}
     * @throws ItemNotFoundException in case item entity is not found in database
     */
    public ItemEntity getItem(UUID itemId) throws ItemNotFoundException {
        return jpaItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found."));
    }

    /**
     * Update item property.
     *
     * @param itemId item id
     * @param key key
     * @param newValue new value
     * @return updated {@link ItemEntity}
     */
    public ItemEntity updateItemProperty(UUID itemId, String key, String newValue) {
        final ItemEntity itemEntity = getItem(itemId);
        final Map<String, ItemPropertiesModel> properties = itemEntity.getProperties();
        if (properties.get(key) == null) {
            properties.put(key, ItemPropertiesModel.builder()
                    .value(newValue)
                    .build());
            return jpaItemRepository.save(itemEntity);
        }
        final String originalValueInDatabase = Optional.ofNullable(properties.get(key).getOriginalValue())
                .orElseGet(() -> properties.get(key).getValue());
        final boolean edited = !originalValueInDatabase.equals(newValue);
        properties.put(key, ItemPropertiesModel.builder()
                .value(newValue)
                .originalValue(edited ? originalValueInDatabase : null)
                .build());
        return jpaItemRepository.save(itemEntity);
    }

}
