package org.dataintegration.service.importdata;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.dataintegration.model.cache.DataIntegrationCache;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.model.BatchConfigModel;
import org.dataintegration.service.CheckpointsService;
import org.dataintegration.service.ImportDataService;
import org.dataintegration.service.ScopesService;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.dataintegration.logger.BatchProcessingLogger.log;

@Service
@RequiredArgsConstructor
class ImportDataServiceImpl implements ImportDataService {

    private final ScopesService scopesService;
    private final CheckpointsService checkpointsService;
    private final BatchProcessingService batchProcessingService;
    private final DataIntegrationCache dataIntegrationCache;
    private final BatchConfigModel batchConfig;

    @SuppressWarnings("checkstyle:MethodLength")
    public boolean importData(Callable<CSVReader> csvReaderCallable, UUID projectId, UUID scopeId, long lineCount) {
        final ScopeEntity scopeEntity = scopesService.get(scopeId);
        final String scopeKey = scopeEntity.getKey();
        if (!scopeKey.toLowerCase().endsWith("csv".toLowerCase())) {
            scopesService.markForDeletion(scopeId);
            log(Level.ERROR, scopeKey, scopeId, "File type is not supported.");
            return false;
        }
        long startTime = System.currentTimeMillis();

        boolean success = false;
        int attempt = 0;

        if (scopeEntity.isDelete()) {
            log(Level.WARN, scopeKey, scopeId, "Scope is marked for deletion, skipping batch processing.");
            return false;
        }
        if (!dataIntegrationCache.getProcessingScopes().add(scopeId)) {
            log(Level.WARN, scopeKey, scopeId, "Scope is already being processed, skipping batch processing.");
            return false;
        }
        if (scopeEntity.isFinished()) {
            log(Level.INFO, scopeKey, scopeId, "Scope was already successfully processed, skipping batch processing.");
            dataIntegrationCache.getProcessingScopes().remove(scopeId);
            return true;
        }

        final int batchSize = checkpointsService.createOrGetBatchSize(scopeEntity, lineCount,  batchConfig.getBatchSize());

        try {
            while (attempt < batchConfig.getBatchRetryScopeMax() && !success
                    && !(dataIntegrationCache.getMarkedForDeletionScopes().contains(scopeId)
                    || dataIntegrationCache.getInterruptingScopes().contains(scopeId))) {
                final int batchRetryScopeMax = batchConfig.getBatchRetryScopeMax();
                attempt++;

                log(Level.INFO, scopeKey, scopeId, "Starting attempt " + attempt + " of " + batchRetryScopeMax + ".");

                success =
                        batchProcessingService.batchProcessing(csvReaderCallable, projectId, scopeEntity, batchSize, startTime,
                                attempt);
            }

            if (!success) {
                log(Level.ERROR, scopeKey, scopeId,
                        "All retries failed. "
                                + "Batch processing aborted. "
                                + "This could be to an error, "
                                + "a manual interruption or because the object got deleted during processing. "
                                + "Please check logs for further information.");
                if (!scopeEntity.isExternal()) {
                    scopesService.markForDeletion(scopeId);
                    checkpointsService.delete(scopeId);
                }
            } else {
                checkpointsService.delete(scopeId);
            }

            return success;
        } catch (Exception ex) {
            log(Level.ERROR, scopeKey, scopeId, "Error occurred: " + Arrays.toString(ex.getStackTrace()));
            return false;
        } finally {
            dataIntegrationCache.getProcessingScopes().remove(scopeId);
            dataIntegrationCache.getInterruptingScopes().remove(scopeId);
        }
    }

}
