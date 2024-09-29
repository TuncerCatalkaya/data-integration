package org.datamigration.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.datamigration.model.ItemStatusModel;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "mapping_item_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MappingItemStatusEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> properties;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemStatusModel status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapping_id", nullable = false)
    private MappingEntity mapping;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemEntity item;

}
