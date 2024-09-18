package org.datamigration.jpa.repository;

import jakarta.transaction.Transactional;
import org.datamigration.jpa.entity.ScopeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaScopeRepository extends JpaRepository<ScopeEntity, UUID> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE ScopeEntity
        SET finished = true
        WHERE id = :scopeId
    """)
    void finish(@Param("scopeId") UUID scopeId);

    @Query("""
        SELECT s.finished FROM ScopeEntity s
        WHERE s.id = :scopeId
    """)
    boolean findFinishedByScopeId(@Param("scopeId") UUID scopeId);

}
