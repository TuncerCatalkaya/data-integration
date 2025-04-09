package org.dataintegration.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dataintegration.model.ItemPropertiesModel;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "item",
        indexes = {
                @Index(name = "idx_item_line_number", columnList = "lineNumber"),
                @Index(name = "idx_item_scope_id", columnList = "scope_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private long lineNumber;

    @NotNull
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, ItemPropertiesModel> properties;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private Set<MappedItemEntity> mappings = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scope_id", nullable = false)
    private ScopeEntity scope;

}
