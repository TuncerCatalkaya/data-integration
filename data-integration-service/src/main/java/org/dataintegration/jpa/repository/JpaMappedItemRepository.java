package org.dataintegration.jpa.repository;

import jakarta.transaction.Transactional;
import org.dataintegration.jpa.entity.MappedItemEntity;
import org.dataintegration.model.ItemStatusModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaMappedItemRepository extends JpaRepository<MappedItemEntity, UUID> {

    @SuppressWarnings("checkstyle:MethodName")
    List<MappedItemEntity> findAllByItem_Id(UUID itemId);

    @SuppressWarnings("checkstyle:MethodName")
    Page<MappedItemEntity> findAllByMapping_Id(UUID mappingId, Pageable pageable);

    @Query("""
        SELECT mi FROM MappedItemEntity mi
        WHERE mi.mapping.id = :mappingId AND mi.status != :itemStatus
        ORDER BY mi.item.lineNumber ASC
    """)
    Page<MappedItemEntity> findAllByMappingIdAndStatusNot(@Param("mappingId") UUID mappingId,
                                                           @Param("itemStatus") ItemStatusModel itemStatus,
                                                           Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        UPDATE MappedItemEntity
        SET status = :itemStatus
        WHERE id IN :mappedItemId
    """)
    void updateStatusById(@Param("mappedItemId") UUID mappedItemId, @Param("itemStatus") ItemStatusModel itemStatus);

    @Modifying
    @Transactional
    @Query("""
        UPDATE MappedItemEntity
        SET errorMessages = :errorMessages
        WHERE id IN :mappedItemId
    """)
    void updateErrorMessagesById(@Param("mappedItemId") UUID mappedItemId, @Param("errorMessages") List<String> errorMessages);

}
