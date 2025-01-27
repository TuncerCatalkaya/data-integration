package org.dataintegration.jpa.repository;

import jakarta.transaction.Transactional;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.model.HeaderModel;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Modifying
    @Transactional
    @Query("""
        UPDATE ScopeEntity
        SET delete = true
        WHERE id = :scopeId
    """)
    void markForDeletion(@Param("scopeId") UUID scopeId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ScopeEntity
        SET headers = :headers
        WHERE id = :scopeId
    """)
    void updateHeaders(@Param("scopeId") UUID scopeId, @Param("headers") Set<HeaderModel> headers);

    List<ScopeEntity> findAllByProject_idAndDeleteFalse(UUID projectId, Sort sort);

    Optional<ScopeEntity> findByProject_IdAndKeyAndDeleteFalse(UUID projectId, String key);

    Optional<ScopeEntity> findByIdAndDeleteFalse(UUID scopeId);

}
