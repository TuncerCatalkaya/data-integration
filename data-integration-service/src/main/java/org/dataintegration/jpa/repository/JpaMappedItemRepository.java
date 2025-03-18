package org.dataintegration.jpa.repository;

import org.dataintegration.jpa.entity.MappedItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaMappedItemRepository extends JpaRepository<MappedItemEntity, UUID> {

    @SuppressWarnings("checkstyle:MethodName")
    List<MappedItemEntity> findAllByItem_Id(UUID itemId);

    @SuppressWarnings("checkstyle:MethodName")
    Page<MappedItemEntity> findAllByMapping_Id(UUID mappingId, Pageable pageable);
    
}
