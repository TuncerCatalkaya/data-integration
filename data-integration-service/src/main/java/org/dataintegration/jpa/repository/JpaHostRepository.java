package org.dataintegration.jpa.repository;

import org.dataintegration.jpa.entity.HostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaHostRepository extends JpaRepository<HostEntity, UUID> {
    @Query("""
        SELECT count(h.id) = 1
        FROM HostEntity h
        WHERE h.url = :url AND NOT (h.id = :hostIdNotIncluded)
    """)
    boolean existsByUrlWithCount(@Param("url") String url, @Param("hostIdNotIncluded") UUID hostIdNotIncluded);

    boolean existsByUrl(String url);
}
