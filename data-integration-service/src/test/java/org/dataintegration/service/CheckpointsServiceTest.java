package org.dataintegration.service;

import org.dataintegration.exception.runtime.CheckpointNotFoundException;
import org.dataintegration.jpa.entity.CheckpointEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.jpa.repository.JpaCheckpointBatchRepository;
import org.dataintegration.jpa.repository.JpaCheckpointRepository;
import org.dataintegration.model.cache.DataIntegrationCache;
import org.dataintegration.usecase.model.CurrentCheckpointStatusResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckpointsServiceTest {

    private static final UUID SCOPE_ID = UUID.randomUUID();

    @Mock
    private JpaCheckpointRepository jpaCheckpointRepository;
    @Mock
    private JpaCheckpointBatchRepository jpaCheckpointBatchRepository;
    @Mock
    private DataIntegrationCache dataIntegrationCache;

    @InjectMocks
    private CheckpointsService subject;

    @Mock
    private ScopeEntity scopeEntity;
    @Mock
    private CheckpointEntity checkpointEntity;

    @Test
    void testCreateOrGetBatchSizeExistingEntity() {
        when(scopeEntity.getId()).thenReturn(SCOPE_ID);
        when(checkpointEntity.getBatchSize()).thenReturn(100);
        when(jpaCheckpointRepository.findByScope_Id(SCOPE_ID)).thenReturn(Optional.of(checkpointEntity));

        final int result = subject.createOrGetBatchSize(scopeEntity, 10, 50);

        assertThat(result).isEqualTo(100);
    }

    @Test
    void testCreateOrGetBatchSizeNewEntity() {
        final int result = subject.createOrGetBatchSize(scopeEntity, 10, 50);
        assertThat(result).isEqualTo(50);
    }

    @Test
    void testGetCurrentCheckpointStatusIsInterruptedAndScopeAlreadyProcessed() {
        when(scopeEntity.getId()).thenReturn(SCOPE_ID);
        when(dataIntegrationCache.getInterruptingScopes()).thenReturn(Set.of(SCOPE_ID));

        final CurrentCheckpointStatusResponseModel result = subject.getCurrentCheckpointStatus(scopeEntity);

        assertThat(result).isNotNull()
                .returns(0L, CurrentCheckpointStatusResponseModel::getBatchesProcessed)
                .returns(0L, CurrentCheckpointStatusResponseModel::getTotalBatches)
                .returns(false, CurrentCheckpointStatusResponseModel::isProcessing)
                .returns(false, CurrentCheckpointStatusResponseModel::isFinished)
                .returns(false, CurrentCheckpointStatusResponseModel::isExternal);
    }

    @Test
    void testGetCurrentCheckpointStatusIsNotInterruptAndScopeAlreadyProcessed() {
        final CurrentCheckpointStatusResponseModel result = subject.getCurrentCheckpointStatus(scopeEntity);

        assertThat(result).isNotNull()
                .returns(-1L, CurrentCheckpointStatusResponseModel::getBatchesProcessed)
                .returns(-1L, CurrentCheckpointStatusResponseModel::getTotalBatches)
                .returns(false, CurrentCheckpointStatusResponseModel::isProcessing)
                .returns(false, CurrentCheckpointStatusResponseModel::isFinished)
                .returns(false, CurrentCheckpointStatusResponseModel::isExternal);
    }

    @Test
    void testGetCurrentCheckpointStatusIsNotInterruptedAndScopeProcessing() {
        when(scopeEntity.getId()).thenReturn(SCOPE_ID);
        when(scopeEntity.getCheckpoint()).thenReturn(checkpointEntity);
        when(jpaCheckpointBatchRepository.countBatchIndexByScopeId(SCOPE_ID)).thenReturn(10L);
        when(checkpointEntity.getTotalBatches()).thenReturn(100L);
        when(dataIntegrationCache.getProcessingScopes()).thenReturn(Set.of(SCOPE_ID));

        final CurrentCheckpointStatusResponseModel result = subject.getCurrentCheckpointStatus(scopeEntity);

        assertThat(result).isNotNull()
                .returns(10L, CurrentCheckpointStatusResponseModel::getBatchesProcessed)
                .returns(100L, CurrentCheckpointStatusResponseModel::getTotalBatches)
                .returns(true, CurrentCheckpointStatusResponseModel::isProcessing)
                .returns(false, CurrentCheckpointStatusResponseModel::isFinished)
                .returns(false, CurrentCheckpointStatusResponseModel::isExternal);
    }

    @Test
    void testIsBatchAlreadyProcessed() {
        when(jpaCheckpointBatchRepository.existsByCheckpoint_ScopeIdAndBatchIndex(SCOPE_ID, 0L)).thenReturn(true);

        final boolean result = subject.isBatchAlreadyProcessed(SCOPE_ID, 0L);

        assertThat(result).isTrue();
    }

    @Test
    void testGetCheckpoint() {
        when(jpaCheckpointRepository.findByScope_Id(SCOPE_ID)).thenReturn(Optional.of(checkpointEntity));

        final CheckpointEntity result = subject.getCheckpoint(SCOPE_ID);

        assertThat(result).isEqualTo(checkpointEntity);
    }

    @Test
    void testGetCheckpointCheckpointNotFoundException() {
        assertThatExceptionOfType(CheckpointNotFoundException.class).isThrownBy(() -> subject.getCheckpoint(SCOPE_ID));
    }

    @Test
    void testDelete() {
        subject.delete(SCOPE_ID);
        verify(jpaCheckpointRepository).deleteByScope_Id(SCOPE_ID);
    }

}