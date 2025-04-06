package org.dataintegration.service;

import org.dataintegration.jpa.entity.MappedItemEntity;
import org.dataintegration.jpa.entity.MappingEntity;
import org.dataintegration.model.ItemPropertiesModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MappedItemsPropertiesServiceTest {

    private static final Map<String, String[]> MAPPING = Map.of(
            "sourceKey12", new String[]{"mappedKey1", "mappedKey2"},
            "sourceKey34", new String[]{"mappedKey3", "mappedKey4"},
            "sourceKey56", new String[]{"mappedKey5", "mappedKey6"}
    );
    private static final Map<String, ItemPropertiesModel> PROPERTIES = Map.of(
            "sourceKey12", ItemPropertiesModel.builder().value("sourceValue12").build(),
            "sourceKey34", ItemPropertiesModel.builder().value("sourceValue34").build(),
            "sourceKey56", ItemPropertiesModel.builder().value("sourceValue56").build(),
            "mappedKey2", ItemPropertiesModel.builder().value("mappedKey2Value").build()
    );

    @InjectMocks
    private MappedItemsPropertiesService subject;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MappedItemEntity mappedItemEntity;
    @Mock
    private MappingEntity mappingEntity;

    @Test
    void testCreateOrGetPropertiesCreateNotInMapping() {
        when(mappedItemEntity.getProperties()).thenReturn(null);
        when(mappedItemEntity.getMapping()).thenReturn(mappingEntity);
        when(mappingEntity.getMapping()).thenReturn(MAPPING);

        final Map<String, ItemPropertiesModel> result =
                subject.createOrGetProperties(mappedItemEntity, "mappedKey", "newValue");

        assertThat(result).isEmpty();
    }

    @Test
    void testCreateOrGetPropertiesCreate() {
        when(mappedItemEntity.getProperties()).thenReturn(null);
        when(mappedItemEntity.getMapping()).thenReturn(mappingEntity);
        when(mappingEntity.getMapping()).thenReturn(MAPPING);
        when(mappedItemEntity.getItem().getProperties()).thenReturn(PROPERTIES);

        final Map<String, ItemPropertiesModel> result =
                subject.createOrGetProperties(mappedItemEntity, "mappedKey1", "newValue");

        assertThat(result).containsEntry("mappedKey1", ItemPropertiesModel.builder()
                .value("newValue")
                .originalValue("sourceValue12")
                .build());
    }

    @Test
    void testCreateOrGetPropertiesGet() {
        when(mappedItemEntity.getProperties()).thenReturn(new HashMap<>(PROPERTIES));
        when(mappedItemEntity.getMapping()).thenReturn(mappingEntity);
        when(mappingEntity.getMapping()).thenReturn(MAPPING);
        when(mappedItemEntity.getItem().getProperties()).thenReturn(PROPERTIES);

        final Map<String, ItemPropertiesModel> result =
                subject.createOrGetProperties(mappedItemEntity, "mappedKey2", "newValue");

        assertThat(result).hasSize(4);
    }

}