package org.dataintegration.jpa.repository;

import org.dataintegration.jpa.entity.HostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaHostRepository extends JpaRepository<HostEntity, UUID> {

}
