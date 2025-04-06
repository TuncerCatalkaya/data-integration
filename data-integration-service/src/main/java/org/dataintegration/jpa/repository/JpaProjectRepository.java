package org.dataintegration.jpa.repository;

import jakarta.transaction.Transactional;
import org.dataintegration.jpa.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE ProjectEntity
        SET delete = true
        WHERE id = :projectId
    """)
    void markForDeletion(@Param("projectId") UUID projectId);

    boolean existsByIdAndCreatedByAndDeleteFalse(UUID projectID, String createdBy);

    Optional<ProjectEntity> findByIdAndCreatedByAndDeleteFalse(UUID projectId, String createdBy);

    Page<ProjectEntity> findAllByCreatedByAndDeleteFalse(String createdBy, Pageable pageable);

}
