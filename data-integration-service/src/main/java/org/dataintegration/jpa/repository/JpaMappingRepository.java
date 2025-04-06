package org.dataintegration.jpa.repository;

import jakarta.transaction.Transactional;
import org.dataintegration.jpa.entity.MappingEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaMappingRepository extends JpaRepository<MappingEntity, UUID> {

    @SuppressWarnings("checkstyle:MethodName")
    List<MappingEntity> findAllByScope_IdAndDeleteFalse(UUID scopeId, Sort sort);

    @Modifying
    @Transactional
    @Query("""
        UPDATE MappingEntity
        SET delete = true
        WHERE id = :mappingId
    """)
    void markForDeletion(@Param("mappingId") UUID mappingId);

}
