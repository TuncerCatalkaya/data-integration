package org.dataintegration.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dataintegration.model.ItemPropertiesModel;
import org.dataintegration.model.ItemStatusModel;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "mapped_item",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mapped_item_item_id_mapping_id", columnNames = {"item_id", "mapping_id"})
        },
        indexes = {
                @Index(name = "idx_mapped_item_item_id", columnList = "item_id"),
                @Index(name = "idx_mapped_item_mapping_id", columnList = "mapping_id"),
                @Index(name = "idx_mapped_item_mapping_id_status", columnList = "mapping_id, status"),
                @Index(name = "idx_mapped_item_item_id_mapping_id", columnList = "mapping_id, item_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MappedItemEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, ItemPropertiesModel> properties;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemStatusModel status;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> errorMessages;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mapping_id", nullable = false)
    private MappingEntity mapping;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemEntity item;

}
