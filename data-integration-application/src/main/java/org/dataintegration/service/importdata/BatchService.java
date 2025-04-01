package org.dataintegration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.dataintegration.model.cache.DataIntegrationCache;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.service.CheckpointsService;
import org.dataintegration.service.ScopesService;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.dataintegration.logger.BatchProcessingLogger.log;

@Service
@RequiredArgsConstructor
class BatchService {

    private final ScopesService scopesService;
    private final CheckpointsService checkpointsService;
    private final DataIntegrationCache dataIntegrationCache;

    boolean checkIfFailedDueToCacheInterruption(ScopeEntity scopeEntity) {
        final String scopeKey = scopeEntity.getKey();
        final UUID scopeId = scopeEntity.getId();
        if (dataIntegrationCache.getMarkedForDeletionScopes().contains(scopeId)) {
            log(Level.WARN, scopeKey, scopeId, "Scope is marked for deletion.");
            return true;
        }
        if (dataIntegrationCache.getInterruptingScopes().contains(scopeId)) {
            log(Level.WARN, scopeKey, scopeId, "Process was interrupted manually.");
            return true;
        }
        return false;
    }

    boolean isBatchAlreadyProcessed(AtomicLong lineCounter, int batchSize, ScopeEntity scopeEntity, AtomicLong batchIndex,
                                           AtomicBoolean batchAlreadyProcessedCache) {
        if (lineCounter.get() % batchSize == 0) {
            final UUID scopeId = scopeEntity.getId();
            if (checkpointsService.isBatchAlreadyProcessed(scopeId, batchIndex.get())) {
                batchAlreadyProcessedCache.set(true);
                final String scopeKey = scopeEntity.getKey();
                log(Level.DEBUG, scopeKey, scopeId, "Batch " + batchIndex.get() + " already processed, skipping batch.");
                return true;
            }
            batchAlreadyProcessedCache.set(false);
        }

        return batchAlreadyProcessedCache.get();
    }

    boolean checkIfBatchProcessingWasSuccessful(ScopeEntity scopeEntity, long startTime, AtomicBoolean failed,
                                                       AtomicLong activeBatchesScope) {
        final String scopeKey = scopeEntity.getKey();
        final UUID scopeId = scopeEntity.getId();
        if (!failed.get() && activeBatchesScope.get() <= 0) {
            scopesService.finish(scopeId);
            long estimatedTime = System.currentTimeMillis() - startTime;
            log(Level.INFO, scopeKey, scopeId, "All batches processed. Total time: " + estimatedTime + " ms.");
            return true;
        }
        return false;
    }

}
