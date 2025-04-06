package org.dataintegration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.dataintegration.model.AsyncBatchConfigModel;
import org.dataintegration.model.BatchConfigModel;
import org.dataintegration.model.BatchProcessingModel;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
class HandleAsyncBatchService {

    private final BatchWaitingService batchWaitingService;
    private final AsyncRetryBatchService asyncRetryBatchService;
    private final AsyncBatchConfigModel asyncBatchConfig;
    private final BatchConfigModel batchConfig;

    private final Object lock = new Object();

    void handleAsyncBatch(BatchProcessingModel batchProcessing, AtomicBoolean failed, AtomicLong activeBatchesScope) {
        final AtomicLong activeBatches = asyncBatchConfig.getActiveBatches();
        synchronized (lock) {
            while (activeBatches.get() >= asyncBatchConfig.getBatchThreads()) {
                batchWaitingService.waitForFullQueue(batchProcessing);
            }
            activeBatches.incrementAndGet();
            activeBatchesScope.incrementAndGet();
        }

        asyncRetryBatchService.retryBatch(batchProcessing, failed, activeBatchesScope, batchConfig.getBatchRetryBatchMax());
    }

}
