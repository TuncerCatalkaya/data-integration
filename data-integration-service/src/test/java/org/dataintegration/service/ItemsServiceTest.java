package org.dataintegration.service;

import org.dataintegration.exception.runtime.ItemNotFoundException;
import org.dataintegration.jpa.entity.ItemEntity;
import org.dataintegration.jpa.repository.JpaItemRepository;
import org.dataintegration.model.ItemPropertiesModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemsServiceTest {

    private static final UUID SCOPE_ID = UUID.randomUUID();
    private static final UUID MAPPING_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final Pageable PAGEABLE = PageRequest.of(2, 100);

    @Mock
    private JpaItemRepository jpaItemRepository;

    @InjectMocks
    private ItemsService subject;

    @Mock
    private Page<ItemEntity> itemEntityPage;
    @Mock
    private List<ItemEntity> itemEntityList;
    @Mock
    private ItemEntity itemEntity;

    @Test
    void testGetAllFilterMappedItemsWithoutSearchText() {
        when(jpaItemRepository.findAllByScopeIdAndMappingIdNotInMappedItems(eq(SCOPE_ID), eq(MAPPING_ID),
                any(Pageable.class))).thenReturn(itemEntityPage);

        final Page<ItemEntity> result = subject.getAll(SCOPE_ID, MAPPING_ID, true, "", "", PAGEABLE);

        assertThat(result).isEqualTo(itemEntityPage);
    }

    @Test
    void testGetAllFilterMappedItemsWithSearchTextAndWithSearchHeader() {
        when(jpaItemRepository.findAllByScopeIdAndMappingIdNotInMappedItemsAndDynamicHeader(eq(SCOPE_ID), eq(MAPPING_ID),
                eq("searchHeader"), eq("searchText"), any(Pageable.class))).thenReturn(itemEntityPage);

        final Page<ItemEntity> result = subject.getAll(SCOPE_ID, MAPPING_ID, true, "searchHeader", "searchText", PAGEABLE);

        assertThat(result).isEqualTo(itemEntityPage);
    }

    @Test
    void testGetAllFilterMappedItemsWithSearchTextAndWithoutSearchHeader() {
        when(jpaItemRepository.findAllByScopeIdAndMappingIdNotInMappedItemsWithFreeTextSearch(eq(SCOPE_ID), eq(MAPPING_ID),
                eq("searchText"), any(Pageable.class))).thenReturn(itemEntityPage);

        final Page<ItemEntity> result = subject.getAll(SCOPE_ID, MAPPING_ID, true, "", "searchText", PAGEABLE);

        assertThat(result).isEqualTo(itemEntityPage);
    }

    @Test
    void testGetAllWithoutSearchText() {
        when(jpaItemRepository.findAllByScopeId(eq(SCOPE_ID), any(Pageable.class))).thenReturn(itemEntityPage);

        final Page<ItemEntity> result = subject.getAll(SCOPE_ID, MAPPING_ID, false, "", "", PAGEABLE);

        assertThat(result).isEqualTo(itemEntityPage);
    }

    @Test
    void testGetAllWithSearchTextAndWithSearchHeader() {
        when(jpaItemRepository.findAllByScopeIdAndDynamicHeader(eq(SCOPE_ID), eq("searchHeader"), eq("searchText"),
                any(Pageable.class))).thenReturn(itemEntityPage);

        final Page<ItemEntity> result = subject.getAll(SCOPE_ID, null, true ,"searchHeader", "searchText", PAGEABLE);

        assertThat(result).isEqualTo(itemEntityPage);
    }

    @Test
    void testGetAllWithSearchTextAndWithoutSearchHeader() {
        when(jpaItemRepository.findAllByScopeIdWithFreeTextSearch(eq(SCOPE_ID), eq("searchText"),
                any(Pageable.class))).thenReturn(itemEntityPage);

        final Page<ItemEntity> result = subject.getAll(SCOPE_ID, null, false, "", "searchText", PAGEABLE);

        assertThat(result).isEqualTo(itemEntityPage);
    }

    @Test
    void testGetAll() {
        when(jpaItemRepository.findAllById(List.of(ITEM_ID))).thenReturn(itemEntityList);

        final List<ItemEntity> result = subject.getAll(List.of(ITEM_ID));

        assertThat(result).isEqualTo(itemEntityList);
    }

    @Test
    void testGetItem() {
        when(jpaItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(itemEntity));

        final ItemEntity result = subject.getItem(ITEM_ID);

        assertThat(result).isEqualTo(itemEntity);
    }

    @Test
    void testGetItemItemNotFoundException() {
        assertThatExceptionOfType(ItemNotFoundException.class).isThrownBy(() -> subject.getItem(ITEM_ID));
    }

    @Test
    void testUpdateItemPropertyItemPropertiesNull() {
        final Map<String, ItemPropertiesModel> properties = new HashMap<>();

        when(itemEntity.getProperties()).thenReturn(properties);
        when(jpaItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(itemEntity));
        when(jpaItemRepository.save(itemEntity)).thenReturn(itemEntity);

        final ItemEntity result = subject.updateItemProperty(ITEM_ID, "key", "newValue");

        assertThat(result).isEqualTo(itemEntity)
                .returns(ItemPropertiesModel.builder()
                        .value("newValue")
                        .build(), i -> i.getProperties().get("key"));
    }

    @Test
    void testUpdateItemPropertyWithOriginalValueAsOriginalValueAndNewValueEqualsOriginalValue() {
        final Map<String, ItemPropertiesModel> properties = new HashMap<>();
        properties.put("key", ItemPropertiesModel.builder()
                        .value("oldValue")
                        .originalValue("originalValue")
                .build());

        when(itemEntity.getProperties()).thenReturn(properties);
        when(jpaItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(itemEntity));
        when(jpaItemRepository.save(itemEntity)).thenReturn(itemEntity);

        final ItemEntity result = subject.updateItemProperty(ITEM_ID, "key", "originalValue");

        assertThat(result).isEqualTo(itemEntity)
                .returns(ItemPropertiesModel.builder()
                        .value("originalValue")
                        .originalValue(null)
                        .build(), i -> i.getProperties().get("key"));
    }

    @Test
    void testUpdateItemPropertyWithOriginalValueAsOriginalValueAndNewValueNotEqualsOriginalValue() {
        final Map<String, ItemPropertiesModel> properties = new HashMap<>();
        properties.put("key", ItemPropertiesModel.builder()
                .value("oldValue")
                .originalValue("originalValue")
                .build());

        when(itemEntity.getProperties()).thenReturn(properties);
        when(jpaItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(itemEntity));
        when(jpaItemRepository.save(itemEntity)).thenReturn(itemEntity);

        final ItemEntity result = subject.updateItemProperty(ITEM_ID, "key", "newValue");

        assertThat(result).isEqualTo(itemEntity)
                .returns(ItemPropertiesModel.builder()
                        .value("newValue")
                        .originalValue("originalValue")
                        .build(), i -> i.getProperties().get("key"));
    }

    @Test
    void testUpdateItemPropertyWithValueAsOriginalValueAndNewValueEqualsOriginalValue() {
        final Map<String, ItemPropertiesModel> properties = new HashMap<>();
        properties.put("key", ItemPropertiesModel.builder()
                .value("oldValue")
                .build());

        when(itemEntity.getProperties()).thenReturn(properties);
        when(jpaItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(itemEntity));
        when(jpaItemRepository.save(itemEntity)).thenReturn(itemEntity);

        final ItemEntity result = subject.updateItemProperty(ITEM_ID, "key", "oldValue");

        assertThat(result).isEqualTo(itemEntity)
                .returns(ItemPropertiesModel.builder()
                        .value("oldValue")
                        .originalValue(null)
                        .build(), i -> i.getProperties().get("key"));
    }

    @Test
    void testUpdateItemPropertyWithValueAsOriginalValueAndNewValueNotEqualsOriginalValue() {
        final Map<String, ItemPropertiesModel> properties = new HashMap<>();
        properties.put("key", ItemPropertiesModel.builder()
                .value("oldValue")
                .build());

        when(itemEntity.getProperties()).thenReturn(properties);
        when(jpaItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(itemEntity));
        when(jpaItemRepository.save(itemEntity)).thenReturn(itemEntity);

        final ItemEntity result = subject.updateItemProperty(ITEM_ID, "key", "newValue");

        assertThat(result).isEqualTo(itemEntity)
                .returns(ItemPropertiesModel.builder()
                        .value("newValue")
                        .originalValue("oldValue")
                        .build(), i -> i.getProperties().get("key"));
    }

}