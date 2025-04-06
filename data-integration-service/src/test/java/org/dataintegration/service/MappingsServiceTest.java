package org.dataintegration.service;

import org.dataintegration.exception.runtime.MappingNotFoundException;
import org.dataintegration.jpa.entity.MappingEntity;
import org.dataintegration.jpa.repository.JpaMappingRepository;
import org.dataintegration.model.DataIntegrationHeaderDataAPIModel;
import org.dataintegration.model.ValidateMappingModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MappingsServiceTest {

    private static final UUID SCOPE_ID = UUID.randomUUID();
    private static final UUID MAPPING_ID = UUID.randomUUID();

    @Mock
    private JpaMappingRepository jpaMappingRepository;
    @Mock
    private MappingsValidationService mappingsValidationService;

    @InjectMocks
    private MappingsService subject;

    @Mock
    private MappingEntity mappingEntity;
    @Mock
    private List<MappingEntity> mappingEntityList;
    @Mock
    private List<DataIntegrationHeaderDataAPIModel> dataIntegrationHeaderDataAPIModelList;
    @Mock
    private ValidateMappingModel validateMappingModel;

    @Test
    void testCreateOrUpdateMapping() {
        when(jpaMappingRepository.save(mappingEntity)).thenReturn(mappingEntity);

        final MappingEntity result = subject.createOrUpdateMapping(mappingEntity);

        assertThat(result).isEqualTo(mappingEntity);
    }

    @Test
    void testGetAll() {
        when(jpaMappingRepository.findAllByScope_IdAndDeleteFalse(SCOPE_ID,
                Sort.by(Sort.Direction.ASC, "createdDate"))).thenReturn(mappingEntityList);

        final List<MappingEntity> result = subject.getAll(SCOPE_ID);

        assertThat(result).isEqualTo(mappingEntityList);
    }

    @Test
    void testGet() {
        when(jpaMappingRepository.findById(MAPPING_ID)).thenReturn(Optional.of(mappingEntity));

        final MappingEntity result = subject.get(MAPPING_ID);

        assertThat(result).isEqualTo(mappingEntity);
    }

    @Test
    void testGetMappingNotFoundException() {
        assertThatExceptionOfType(MappingNotFoundException.class).isThrownBy(() -> subject.get(MAPPING_ID));
    }

    @Test
    void testMarkForDeletion() {
        subject.markForDeletion(MAPPING_ID);
        verify(jpaMappingRepository).markForDeletion(MAPPING_ID);
    }

    @Test
    void testMarkForDeletionByScope() {
        final List<MappingEntity> mappingEntities = List.of(new MappingEntity());

        when(jpaMappingRepository.findAllByScope_IdAndDeleteFalse(SCOPE_ID, Sort.unsorted())).thenReturn(mappingEntities);

        subject.markForDeletionByScope(SCOPE_ID);

        verify(jpaMappingRepository).saveAll(mappingEntities);
    }

    @Test
    void testValidateMapping() {
        when(mappingsValidationService.validateMapping(anyMap(), anySet())).thenReturn(validateMappingModel);

        subject.validateMapping(MAPPING_ID, Map.of(), dataIntegrationHeaderDataAPIModelList);

        verify(mappingsValidationService).validateMappingErrorHandler(anyString(), eq(validateMappingModel));
    }

}