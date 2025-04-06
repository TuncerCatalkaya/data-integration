package org.dataintegration.service;

import org.dataintegration.exception.runtime.MappedItemFrozenException;
import org.dataintegration.exception.runtime.MappedItemNotFoundException;
import org.dataintegration.jpa.entity.MappedItemEntity;
import org.dataintegration.jpa.entity.MappingEntity;
import org.dataintegration.jpa.repository.JpaMappedItemRepository;
import org.dataintegration.model.ItemPropertiesModel;
import org.dataintegration.model.ItemStatusModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MappedItemsServiceTest {

    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final UUID MAPPED_ITEM_ID = UUID.randomUUID();
    private static final UUID MAPPING_ID = UUID.randomUUID();
    private static final Pageable PAGEABLE = PageRequest.of(2, 100);
    private static final Map<String, ItemPropertiesModel> PROPERTIES = Map.of(
            "sourceKey12", ItemPropertiesModel.builder().value("sourceValue12").build(),
            "sourceKey34", ItemPropertiesModel.builder().value("sourceValue34").build(),
            "sourceKey56", ItemPropertiesModel.builder().value("sourceValue56").build(),
            "mappedKey2", ItemPropertiesModel.builder().value("mappedKey2Value").build()
    );

    @Mock
    private JpaMappedItemRepository jpaMappedItemRepository;
    @Mock
    private MappedItemsPropertiesService mappedItemsPropertiesService;

    @InjectMocks
    private MappedItemsService subject;

    @Mock
    private List<MappedItemEntity> mappedItemEntityList;
    @Mock
    private Page<MappedItemEntity> mappedItemEntityPage;
    @Mock
    private List<UUID> mappedItemUUIDList;
    @Mock
    private List<String> errorMessages;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MappedItemEntity mappedItemEntity;

    @Test
    void testApplyMapping() {
        subject.applyMapping(mappedItemEntityList);
        verify(jpaMappedItemRepository).saveAll(mappedItemEntityList);
    }

    @Test
    void testGetItemsWithMappings() {
        final List<UUID> itemIds = List.of(ITEM_ID);

        final MappedItemEntity mappedItem = new MappedItemEntity();
        mappedItem.setId(ITEM_ID);
        final MappingEntity mappingEntity = new MappingEntity();
        mappingEntity.setId(MAPPING_ID);
        mappedItem.setMapping(mappingEntity);

        when(jpaMappedItemRepository.findAllByItem_Id(ITEM_ID)).thenReturn(List.of(mappedItem));

        final Map<UUID, List<UUID>> result = subject.getItemsWithMappings(itemIds);

        assertThat(result).containsEntry(ITEM_ID, List.of(MAPPING_ID));
    }

    @Test
    void testGetByMappingFilterIntegratedItems() {
        when(jpaMappedItemRepository.findAllByMapping_IdAndStatusNot(eq(MAPPED_ITEM_ID), eq(ItemStatusModel.INTEGRATED),
                any(Pageable.class))).thenReturn(mappedItemEntityPage);

        final Page<MappedItemEntity> result = subject.getByMapping(MAPPED_ITEM_ID, true, PAGEABLE);

        assertThat(result).isEqualTo(mappedItemEntityPage);
    }

    @Test
    void testGetByMapping() {
        when(jpaMappedItemRepository.findAllByMapping_Id(eq(MAPPED_ITEM_ID), any(Pageable.class))).thenReturn(
                mappedItemEntityPage);

        final Page<MappedItemEntity> result = subject.getByMapping(MAPPED_ITEM_ID, false, PAGEABLE);

        assertThat(result).isEqualTo(mappedItemEntityPage);
    }

    @Test
    void testGetByMappedItemIds() {
        when(jpaMappedItemRepository.findAllById(mappedItemUUIDList)).thenReturn(mappedItemEntityList);

        final List<MappedItemEntity> result = subject.getByMappedItemIds(mappedItemUUIDList);

        assertThat(result).isEqualTo(mappedItemEntityList);
    }

    @Test
    void testUpdateMappedItemStatusByMappedItemId() {
        subject.updateMappedItemStatusByMappedItemId(MAPPED_ITEM_ID, ItemStatusModel.INTEGRATED);
        verify(jpaMappedItemRepository).updateStatusById(MAPPED_ITEM_ID, ItemStatusModel.INTEGRATED);
    }

    @Test
    void testUpdateErrorMessagesByMappedItemId() {
        subject.updateErrorMessagesByMappedItemId(MAPPED_ITEM_ID, errorMessages);
        verify(jpaMappedItemRepository).updateErrorMessagesById(MAPPED_ITEM_ID, errorMessages);
    }

    @Test
    void testGetMappedItem() {
        when(jpaMappedItemRepository.findById(MAPPED_ITEM_ID)).thenReturn(Optional.of(mappedItemEntity));

        final MappedItemEntity result = subject.getMappedItem(MAPPED_ITEM_ID);

        assertThat(result).isEqualTo(mappedItemEntity);
    }

    @Test
    void testGetMappedItemMappedItemNotFoundException() {
        assertThatExceptionOfType(MappedItemNotFoundException.class).isThrownBy(() -> subject.getMappedItem(MAPPED_ITEM_ID));
    }

    @Test
    void testUpdateMappedItemProperty() {
        final MappedItemEntity mappedItem = new MappedItemEntity();

        when(jpaMappedItemRepository.findById(MAPPED_ITEM_ID)).thenReturn(Optional.of(mappedItem));
        when(mappedItemsPropertiesService.createOrGetProperties(mappedItem, "mappedKey2", "newValue")).thenReturn(
                new HashMap<>(PROPERTIES));
        when(jpaMappedItemRepository.save(mappedItem)).thenReturn(mappedItem);

        final MappedItemEntity result = subject.updateMappedItemProperty(MAPPED_ITEM_ID, "mappedKey2", "newValue");

        assertThat(result).isEqualTo(mappedItem);
        assertThat(result.getProperties()).containsEntry("mappedKey2", ItemPropertiesModel.builder()
                .value("newValue")
                .build());
    }

    @Test
    void testUpdateMappedItemPropertyNull() {
        final MappedItemEntity mappedItem = new MappedItemEntity();

        when(jpaMappedItemRepository.findById(MAPPED_ITEM_ID)).thenReturn(Optional.of(mappedItem));
        when(mappedItemsPropertiesService.createOrGetProperties(mappedItem, "mappedKey2", null)).thenReturn(
                new HashMap<>(PROPERTIES));
        when(jpaMappedItemRepository.save(mappedItem)).thenReturn(mappedItem);

        final MappedItemEntity result = subject.updateMappedItemProperty(MAPPED_ITEM_ID, "mappedKey2", null);

        assertThat(result).isEqualTo(mappedItem);
        assertThat(result.getProperties()).containsEntry("mappedKey2", null);
    }

    @Test
    void testUpdateMappedItemPropertyMappedItemFrozenException() {
        final MappedItemEntity mappedItem = new MappedItemEntity();
        mappedItem.setStatus(ItemStatusModel.INTEGRATED);

        when(jpaMappedItemRepository.findById(MAPPED_ITEM_ID)).thenReturn(Optional.of(mappedItem));

        assertThatExceptionOfType(MappedItemFrozenException.class).isThrownBy(
                () -> subject.updateMappedItemProperty(MAPPED_ITEM_ID, "mappedKey2", "newValue"));
    }

    @Test
    void testDeleteMappedItems() {
        subject.deleteMappedItems(mappedItemUUIDList);
        verify(jpaMappedItemRepository).deleteAllById(mappedItemUUIDList);
    }

}