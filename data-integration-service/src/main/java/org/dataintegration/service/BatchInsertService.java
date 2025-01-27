package org.dataintegration.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.CheckpointBatchEntity;
import org.dataintegration.jpa.entity.CheckpointEntity;
import org.dataintegration.jpa.repository.JpaCheckpointBatchRepository;
import org.dataintegration.jpa.repository.JpaItemRepository;
import org.dataintegration.model.BatchProcessingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchInsertService {

    private final JpaItemRepository jpaItemRepository;
    private final JpaCheckpointBatchRepository jpaCheckpointBatchRepository;

    @Transactional
    public void insertBatch(BatchProcessingModel batchProcessing, CheckpointEntity checkpointEntity) {
        jpaItemRepository.saveAll(batchProcessing.getBatch());

        final CheckpointBatchEntity checkpointBatchEntity = new CheckpointBatchEntity();
        checkpointBatchEntity.setBatchIndex(batchProcessing.getBatchIndex());
        checkpointBatchEntity.setCheckpoint(checkpointEntity);
        jpaCheckpointBatchRepository.save(checkpointBatchEntity);
    }

}
