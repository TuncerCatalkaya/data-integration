package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchWaitingConfigModel {

    private long batchRetryScopeDelayMs;

    private long batchRetryBatchDelayMs;

    private long batchWaitForFullQueueDelayMs;
    private long batchWaitForBatchesToFinishDelayMs;

}
