package org.dataintegration.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "checkpoint_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckpointBatchEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private long batchIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    private CheckpointEntity checkpoint;

}
