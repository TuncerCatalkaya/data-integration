package org.dataintegration.jpa.repository;

import jakarta.transaction.Transactional;
import org.dataintegration.jpa.entity.HostEntity;
import org.dataintegration.model.DataIntegrationHeaderDataAPIModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaHostRepository extends JpaRepository<HostEntity, UUID> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE HostEntity
        SET headers = :headers
        WHERE id = :hostId
    """)
    void updateHeadersByHostId(@Param("hostId") UUID hostId, @Param("headers") List<DataIntegrationHeaderDataAPIModel> headers);

}
