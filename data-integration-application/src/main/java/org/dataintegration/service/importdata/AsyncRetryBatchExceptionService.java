package org.dataintegration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.dataintegration.model.BatchProcessingModel;
import org.slf4j.event.Level;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.dataintegration.logger.BatchProcessingLogger.log;

@RequiredArgsConstructor
class AsyncRetryBatchExceptionService {

    private final AsyncRetryBatchService asyncRetryBatchService;
    private final BatchWaitingService batchWaitingService;

    void handleException(BatchProcessingModel batchProcessing, AtomicBoolean failed, AtomicLong activeBatchesScope,
                                int remainingRetries, Throwable ex) {
        final String scopeKey = batchProcessing.getScopeKey();
        final UUID scopeId = batchProcessing.getScopeId();
        if (ex != null) {
            final AtomicBoolean fatal = new AtomicBoolean(false);
            final String errorPrefix = "Error during batch " + batchProcessing.getBatchIndex() + ". ";
            log(Level.ERROR, scopeKey, scopeId, errorPrefix + Arrays.toString(ex.getStackTrace()));
            if (isFatalException(ex.getCause())) {
                fatal.set(true);
                log(Level.ERROR, scopeKey, scopeId, errorPrefix + "Error is fatal, batch retries will be skipped.");
            }
            if (!fatal.get() && remainingRetries > 1) {
                log(Level.WARN, scopeKey, scopeId, errorPrefix + "Retrying... Remaining retries: " + (remainingRetries - 1));
                batchWaitingService.batchRetryDelay();
                asyncRetryBatchService.retryBatch(batchProcessing, failed, activeBatchesScope, remainingRetries - 1);
            } else {
                log(Level.ERROR, scopeKey, scopeId, errorPrefix + "All batch retries are used up.");
                failed.set(true);
            }
        } else {
            log(Level.DEBUG, scopeKey, scopeId, "Processed batch " + batchProcessing.getBatchIndex());
        }
    }

    private boolean isFatalException(Throwable cause) {
        return cause instanceof CannotCreateTransactionException || cause instanceof DataAccessResourceFailureException;
    }

}
