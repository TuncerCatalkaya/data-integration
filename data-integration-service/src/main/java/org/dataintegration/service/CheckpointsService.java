package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.cache.DataIntegrationCache;
import org.dataintegration.exception.runtime.CheckpointNotFoundException;
import org.dataintegration.jpa.entity.CheckpointEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.jpa.repository.JpaCheckpointBatchRepository;
import org.dataintegration.jpa.repository.JpaCheckpointRepository;
import org.dataintegration.usecase.model.CurrentCheckpointStatusResponseModel;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckpointsService {

    private final JpaCheckpointRepository jpaCheckpointRepository;
    private final JpaCheckpointBatchRepository jpaCheckpointBatchRepository;
    private final DataIntegrationCache dataIntegrationCache;

    public int createOrGetCheckpointBy(ScopeEntity scopeEntity, long lineCount, int batchSize) {
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

    public boolean isBatchAlreadyProcessed(UUID scopeId, long batchIndex) {
        return jpaCheckpointBatchRepository.existsByCheckpoint_ScopeIdAndBatchIndex(scopeId, batchIndex);
    }

    public CheckpointEntity getCheckpoint(UUID scopeId) {
        return jpaCheckpointRepository.findByScope_Id(scopeId)
                .orElseThrow(() -> new CheckpointNotFoundException("Checkpoint of scope " + scopeId + " not found."));
    }

    public void deleteByScopeId(UUID scopeId) {
        jpaCheckpointRepository.deleteByScope_Id(scopeId);
    }
}
