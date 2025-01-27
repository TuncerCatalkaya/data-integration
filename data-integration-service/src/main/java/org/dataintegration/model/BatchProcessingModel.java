package org.dataintegration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dataintegration.jpa.entity.ItemEntity;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchProcessingModel {

    private UUID projectId;
    private UUID scopeId;
    private String scopeKey;
    private long batchIndex;
    private int batchSize;

    private List<ItemEntity> batch;

}
