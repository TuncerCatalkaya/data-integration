package org.dataintegration.service.importdata;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.ItemEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.model.HeaderModel;
import org.dataintegration.service.ScopesService;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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

    @SuppressWarnings("checkstyle:MethodLength")
    boolean batchProcessing(Callable<CSVReader> csvReaderCallable, UUID projectId, ScopeEntity scopeEntity,
                            int batchSize, long startTime, int attempt) {
        final String scopeKey = scopeEntity.getKey();
        final UUID scopeId = scopeEntity.getId();
        try (final CSVReader csvReader = csvReaderCallable.call()) {

            final List<ItemEntity> batch = new ArrayList<>();
            final AtomicLong batchIndex = new AtomicLong(1);

            final AtomicLong activeBatchesScope = new AtomicLong(0);
            final AtomicBoolean failed = new AtomicBoolean(false);

            final LinkedHashSet<HeaderModel> headers = Arrays.stream(csvReader.readNext())
                    .map(header -> HeaderModel.builder()
                            .id(header)
                            .display(header)
                            .hidden(false)
                            .build())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (itemCreationService.isHeaderValid(headers)) {
                if (scopeEntity.getHeaders() == null) {
                    scopeEntity.setHeaders(scopesService.updateHeaders(scopeId, headers));
                }
                log(Level.INFO, scopeKey, scopeId, "Starting to process.");
            } else {
                log(Level.ERROR, scopeKey, scopeId, "CSV header is invalid, stopping batch processing.");
                failed.set(true);
            }

            final AtomicBoolean batchAlreadyProcessedCache = new AtomicBoolean(false);
            final AtomicLong lineCounter = new AtomicLong(-1);

            String[] line;
            while (!failed.get() && (line = csvReader.readNext()) != null) {
                failed.set(batchService.checkIfFailedDueToCacheInterruption(scopeEntity));
                batchIndex.set((lineCounter.incrementAndGet() / batchSize) + 1);

                if (batchService.isBatchAlreadyProcessed(lineCounter, batchSize, scopeEntity, batchIndex,
                        batchAlreadyProcessedCache)) {
                    continue;
                }

                final ItemEntity item = itemCreationService.createItemEntity(line, scopeEntity, lineCounter.get());
                batch.add(item);

                handleBatchService.handleFullBatch(projectId, batch, batchSize, scopeEntity, batchIndex, failed,
                        activeBatchesScope);
            }

            handleBatchService.handleLastBatch(projectId, batch, scopeEntity, batchIndex, batchSize, failed, activeBatchesScope);

            while (activeBatchesScope.get() > 0) {
                batchWaitingService.waitForRemainingBatchesToFinish(scopeEntity);
            }

            if (batchService.checkIfBatchProcessingWasSuccessful(scopeEntity, startTime, failed, activeBatchesScope)) {
                return true;
            }

        } catch (Exception ex) {
            log(Level.ERROR, scopeKey, scopeId, "Attempt " + attempt + " failed: " + Arrays.toString(ex.getStackTrace()));
            batchWaitingService.scopeRetryDelay(attempt);
        }
        return false;
    }

}
