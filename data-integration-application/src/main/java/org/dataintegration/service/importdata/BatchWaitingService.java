package org.dataintegration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.model.BatchConfigModel;
import org.dataintegration.model.BatchProcessingModel;
import org.dataintegration.model.BatchWaitingConfigModel;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.dataintegration.logger.BatchProcessingLogger.log;

@Service
@RequiredArgsConstructor
class BatchWaitingService {

    private final BatchConfigModel batchConfig;
    private final BatchWaitingConfigModel batchWaitingConfig;

    void scopeRetryDelay(int attempt) {
        if (attempt < batchConfig.getBatchRetryScopeMax()) {
            try {
                Thread.sleep(batchWaitingConfig.getBatchRetryScopeDelayMs());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void batchRetryDelay() {
        try {
            Thread.sleep(batchWaitingConfig.getBatchRetryBatchDelayMs());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    void waitForFullQueue(BatchProcessingModel batchProcessing) {
        try {
            final String scopeKey = batchProcessing.getScopeKey();
            final UUID scopeId = batchProcessing.getScopeId();
            log(Level.TRACE, scopeKey, scopeId, "Queue is full, waiting until a batch is completed...");
            Thread.sleep(batchWaitingConfig.getBatchWaitForFullQueueDelayMs());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    void waitForRemainingBatchesToFinish(ScopeEntity scopeEntity) {
        try {
            final String scopeKey = scopeEntity.getKey();
            final UUID scopeId = scopeEntity.getId();
            log(Level.TRACE, scopeKey, scopeId, "Waiting until remaining batches are completed...");
            Thread.sleep(batchWaitingConfig.getBatchWaitForBatchesToFinishDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
