package org.dataintegration.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.dataintegration.model.AsyncBatchConfigModel;
import org.dataintegration.model.BatchConfigModel;
import org.dataintegration.model.BatchWaitingConfigModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Configuration
public class ImportDataBatchConfig {

    @Value("${batch.threads}")
    private int batchThreads;
    @Value("${batch.size}")
    private int batchSize;

    @Value("${batch.retry.scope.max}")
    private int batchRetryScopeMax;
    @Value("${batch.retry.scope.delayMs}")
    private long batchRetryScopeDelayMs;

    @Value("${batch.retry.batch.max}")
    private int batchRetryBatchMax;
    @Value("${batch.retry.batch.delayMs}")
    private long batchRetryBatchDelayMs;

    @Value("${batch.waitForFullQueue.delayMs}")
    private long batchWaitForFullQueueDelayMs;
    @Value("${batch.waitForBatchesToFinish.delayMs}")
    private long batchWaitForBatchesToFinishDelayMs;

    private ExecutorService executorService;
    private AtomicLong activeBatches;

    @PostConstruct
    void configure() {
        executorService = new ThreadPoolExecutor(
                batchThreads,
                batchThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(batchThreads),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        activeBatches = new AtomicLong(0);
    }

    @PreDestroy
    void shutdown() {
        log.info("[Import Data Executor Service] Shutting down...");
        executorService.shutdown();
        log.info("[Import Data Executor Service] Shut down completed.");
    }

    @Bean
    AsyncBatchConfigModel asyncBatchModel() {
        return AsyncBatchConfigModel.builder()
                .executorService(executorService)
                .activeBatches(activeBatches)
                .batchThreads(batchThreads)
                .build();
    }

    @Bean
    BatchConfigModel batchModel() {
        return BatchConfigModel.builder()
                .batchSize(batchSize)
                .batchRetryScopeMax(batchRetryScopeMax)
                .batchRetryBatchMax(batchRetryBatchMax)
                .build();
    }

    @Bean
    BatchWaitingConfigModel batchWaitingModel() {
        return BatchWaitingConfigModel.builder()
                .batchRetryScopeDelayMs(batchRetryScopeDelayMs)
                .batchRetryBatchDelayMs(batchRetryBatchDelayMs)
                .batchWaitForFullQueueDelayMs(batchWaitForFullQueueDelayMs)
                .batchWaitForBatchesToFinishDelayMs(batchWaitForBatchesToFinishDelayMs)
                .build();
    }

}
