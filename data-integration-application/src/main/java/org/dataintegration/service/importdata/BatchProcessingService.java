package org.dataintegration.service.importdata;

import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.ItemEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.model.HeaderModel;
import org.dataintegration.service.ScopesService;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.dataintegration.logger.BatchProcessingLogger.log;

@Service
@RequiredArgsConstructor
class BatchProcessingService {

    private final ScopesService scopesService;
    private final HandleBatchService handleBatchService;
    private final BatchService batchService;
    private final ItemCreationService itemCreationService;
    private final BatchWaitingService batchWaitingService;

    boolean batchProcessing(Callable<InputStream> inputStreamCallable, UUID projectId, ScopeEntity scopeEntity,
                                   int batchSize, long startTime, int attempt, char delimiter) {
        final String scopeKey = scopeEntity.getKey();
        final UUID scopeId = scopeEntity.getId();
        try (final InputStream inputStream = inputStreamCallable.call();
             final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            final List<ItemEntity> batch = new ArrayList<>();
            final AtomicLong batchIndex = new AtomicLong(1);
            final AtomicLong lineCounter = new AtomicLong(-1);

            final AtomicLong activeBatchesScope = new AtomicLong(0);
            final AtomicBoolean failed = new AtomicBoolean(false);

            final AtomicBoolean batchAlreadyProcessedCache = new AtomicBoolean(false);

            final String firstLine = reader.readLine();
            final LinkedHashSet<HeaderModel> headers = Splitter.on(delimiter).splitToStream(firstLine)
                    .map(HeaderModel::new)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (itemCreationService.isHeaderValid(headers)) {
                if (scopeEntity.getHeaders() == null) {
                    scopesService.updateHeaders(scopeId, headers);
                    scopeEntity.setHeaders(headers);
                }
                log(Level.INFO, scopeKey, scopeId, "Starting to process.");
            } else {
                log(Level.ERROR, scopeKey, scopeId, "CSV header is invalid, stopping batch processing.");
                failed.set(true);
            }

            reader.lines()
                    .takeWhile(line -> !failed.get())
                    .forEach(line -> {
                        failed.set(batchService.checkIfFailedDueToCacheInterruption(scopeEntity));
                        batchIndex.set((lineCounter.incrementAndGet() / batchSize) + 1);

                        if (batchService.isBatchAlreadyProcessed(lineCounter, batchSize, scopeEntity, batchIndex,
                                batchAlreadyProcessedCache)) {
                            return;
                        }

                        final ItemEntity itemEntity =
                                itemCreationService.createItemEntity(line, scopeEntity, headers, lineCounter.get(), delimiter);
                        batch.add(itemEntity);

                        handleBatchService.handleFullBatch(projectId, batch, batchSize, scopeEntity, batchIndex, failed,
                                activeBatchesScope);
                    });

            handleBatchService.handleLastBatch(projectId, batch, scopeEntity, batchIndex, batchSize, failed, activeBatchesScope);

            while (activeBatchesScope.get() > 0) {
                batchWaitingService.waitForRemainingBatchesToFinish(scopeEntity);
            }

            if (batchService.checkIfBatchProcessingWasSuccessful(scopeEntity, startTime, failed, activeBatchesScope)) {
                return true;
            }

        } catch (Exception ex) {
            log(Level.ERROR, scopeKey, scopeId, "Attempt " + attempt + " failed: " + ex.getMessage());
            batchWaitingService.scopeRetryDelay(attempt);
        }
        return false;
    }

}
