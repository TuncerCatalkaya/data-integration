package org.dataintegration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.dataintegration.model.AsyncBatchConfigModel;
import org.dataintegration.model.BatchProcessingModel;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
class AsyncRetryBatchService {

    private final AsyncBatchService asyncBatchService;
    private final BatchWaitingService batchWaitingService;
    private final AsyncBatchConfigModel asyncBatchConfig;

    void retryBatch(BatchProcessingModel batchProcessing, AtomicBoolean failed, AtomicLong activeBatchesScope,
                           int remainingRetries) {
        final AtomicLong activeBatches = asyncBatchConfig.getActiveBatches();
        asyncBatchService.processBatchAsync(batchProcessing).whenComplete((result, ex) -> {
            activeBatches.decrementAndGet();
            activeBatchesScope.decrementAndGet();
            if (activeBatches.get() < 0) {
                activeBatches.set(0);
            }
            if (activeBatchesScope.get() < 0) {
                activeBatchesScope.set(0);
            }
            final AsyncRetryBatchExceptionService asyncRetryBatchExceptionService =
                    new AsyncRetryBatchExceptionService(this, batchWaitingService);
            asyncRetryBatchExceptionService.handleException(batchProcessing, failed, activeBatchesScope, remainingRetries, ex);
        });
    }
}
