package org.dataintegration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.CheckpointEntity;
import org.dataintegration.model.AsyncBatchConfigModel;
import org.dataintegration.model.BatchProcessingModel;
import org.dataintegration.service.ItemBatchInsertService;
import org.dataintegration.service.CheckpointsService;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
class AsyncBatchService {

    private final CheckpointsService checkpointsService;
    private final ItemBatchInsertService itemBatchInsertService;
    private final AsyncBatchConfigModel asyncBatchConfig;

    CompletableFuture<Void> processBatchAsync(BatchProcessingModel batchProcessing) {
        return CompletableFuture.runAsync(() -> {
            final CheckpointEntity checkpointEntity = checkpointsService.getCheckpoint(batchProcessing.getScopeId());
            itemBatchInsertService.insertBatch(batchProcessing, checkpointEntity);
        }, asyncBatchConfig.getExecutorService());
    }

}
