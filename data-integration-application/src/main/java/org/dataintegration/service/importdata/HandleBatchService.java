package org.dataintegration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.ItemEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.model.BatchProcessingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
class HandleBatchService {

    private final HandleAsyncBatchService handleAsyncBatchService;

    void handleFullBatch(UUID projectId, List<ItemEntity> batch, int batchSize, ScopeEntity scopeEntity,
                                AtomicLong batchIndex, AtomicBoolean failed, AtomicLong activeBatchesScope) {
        if (batch.size() >= batchSize) {
            final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                    .projectId(projectId)
                    .scopeId(scopeEntity.getId())
                    .scopeKey(scopeEntity.getKey())
                    .batchIndex(batchIndex.get())
                    .batchSize(batchSize)
                    .batch(new ArrayList<>(batch))
                    .build();
            handleAsyncBatchService.handleAsyncBatch(batchProcessing, failed, activeBatchesScope);
            batch.clear();
        }
    }

    void handleLastBatch(UUID projectId, List<ItemEntity> batch, ScopeEntity scopeEntity,
                                AtomicLong batchIndex, int batchSize, AtomicBoolean failed, AtomicLong activeBatchesScope) {
        if (!batch.isEmpty() && !failed.get()) {
            final BatchProcessingModel batchProcessing = BatchProcessingModel.builder()
                    .projectId(projectId)
                    .scopeId(scopeEntity.getId())
                    .scopeKey(scopeEntity.getKey())
                    .batchIndex(batchIndex.get())
                    .batchSize(batchSize)
                    .batch(new ArrayList<>(batch))
                    .build();
            handleAsyncBatchService.handleAsyncBatch(batchProcessing, failed, activeBatchesScope);
        }
    }
}
