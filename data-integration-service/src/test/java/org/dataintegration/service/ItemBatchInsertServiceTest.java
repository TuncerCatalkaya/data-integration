package org.dataintegration.service;

import org.dataintegration.jpa.entity.CheckpointBatchEntity;
import org.dataintegration.jpa.entity.CheckpointEntity;
import org.dataintegration.jpa.repository.JpaCheckpointBatchRepository;
import org.dataintegration.jpa.repository.JpaItemRepository;
import org.dataintegration.model.BatchProcessingModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ItemBatchInsertServiceTest {

    @Mock
    private JpaItemRepository jpaItemRepository;
    @Mock
    private JpaCheckpointBatchRepository jpaCheckpointBatchRepository;

    @InjectMocks
    private ItemBatchInsertService subject;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BatchProcessingModel batchProcessing;
    @Mock
    private CheckpointEntity checkpointEntity;

    @Test
    void testInsertBatch() {
        subject.insertBatch(batchProcessing, checkpointEntity);

        verify(jpaItemRepository).saveAll(batchProcessing.getBatch());
        verify(jpaCheckpointBatchRepository).save(any(CheckpointBatchEntity.class));
    }

}