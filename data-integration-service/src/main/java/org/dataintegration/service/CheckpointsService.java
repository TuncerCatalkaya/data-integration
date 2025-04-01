package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.CheckpointNotFoundException;
import org.dataintegration.jpa.entity.CheckpointEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.jpa.repository.JpaCheckpointBatchRepository;
import org.dataintegration.jpa.repository.JpaCheckpointRepository;
import org.dataintegration.model.cache.DataIntegrationCache;
import org.dataintegration.usecase.model.CurrentCheckpointStatusResponseModel;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for checkpointing.
 */
@Service
@RequiredArgsConstructor
public class CheckpointsService {

    private final JpaCheckpointRepository jpaCheckpointRepository;
    private final JpaCheckpointBatchRepository jpaCheckpointBatchRepository;
    private final DataIntegrationCache dataIntegrationCache;

    /**
     * Create or get batch size from checkpoint entity by scope entity. Also creates or gets checkpoint entity by scope entity.
     *
     * @param scopeEntity {@link ScopeEntity}
     * @param lineCount line count (current line)
     * @param batchSize batch size (size of a batch)
     * @return batch size (either from database (checkpoint entity) if already exists or the inputted batch size when creating)
     */
    public int createOrGetBatchSize(ScopeEntity scopeEntity, long lineCount, int batchSize) {
        return jpaCheckpointRepository.findByScope_Id(scopeEntity.getId())
                .map(CheckpointEntity::getBatchSize)
                .orElseGet(() -> {
                    final CheckpointEntity checkpointEntity = new CheckpointEntity();
                    checkpointEntity.setScope(scopeEntity);
                    checkpointEntity.setBatchSize(batchSize);
                    checkpointEntity.setTotalBatches((long) Math.ceil((double) lineCount / batchSize));
                    jpaCheckpointRepository.save(checkpointEntity);
                    return batchSize;
                });
    }

    /**
     * Get current checkpoint status by scope entity.
     *
     * @param scopeEntity {@link ScopeEntity}
     * @return {@link CurrentCheckpointStatusResponseModel} check code to see possibilities of different statuses
     */
    public CurrentCheckpointStatusResponseModel getCurrentCheckpointStatus(ScopeEntity scopeEntity) {
        final boolean isInterrupted = dataIntegrationCache.getInterruptingScopes().contains(scopeEntity.getId());
        long batchesProcessed = isInterrupted ? 0 : -1;
        long totalBatches = isInterrupted ? 0 : -1;
        if (scopeEntity.getCheckpoint() != null) {
            batchesProcessed = jpaCheckpointBatchRepository.countBatchIndexByScopeId(scopeEntity.getId());
            totalBatches = scopeEntity.getCheckpoint().getTotalBatches();
        }
        return CurrentCheckpointStatusResponseModel.builder()
                .batchesProcessed(batchesProcessed)
                .totalBatches(totalBatches)
                .processing(dataIntegrationCache.getProcessingScopes().contains(scopeEntity.getId()))
                .finished(scopeEntity.isFinished())
                .external(scopeEntity.isExternal())
                .build();
    }

    /**
     * Checks if a batch is already processed by scope id and the batch index.
     *
     * @param scopeId scope id from {@link ScopeEntity}
     * @param batchIndex batch index (counter for each batch)
     * @return true if that batch is already processed and false if not
     */
    public boolean isBatchAlreadyProcessed(UUID scopeId, long batchIndex) {
        return jpaCheckpointBatchRepository.existsByCheckpoint_ScopeIdAndBatchIndex(scopeId, batchIndex);
    }

    /**
     * Get checkpoint entity by scope id.
     *
     * @param scopeId scope id from {@link ScopeEntity}
     * @return {@link CheckpointEntity}
     * @throws CheckpointNotFoundException in case checkpoint entity is not found in database
     */
    public CheckpointEntity getCheckpoint(UUID scopeId) throws CheckpointNotFoundException {
        return jpaCheckpointRepository.findByScope_Id(scopeId)
                .orElseThrow(() -> new CheckpointNotFoundException("Checkpoint of scope " + scopeId + " not found."));
    }

    /**
     * Delete checkpoint by scope id.
     *
     * @param scopeId scope id
     */
    public void delete(UUID scopeId) {
        jpaCheckpointRepository.deleteByScope_Id(scopeId);
    }
}
