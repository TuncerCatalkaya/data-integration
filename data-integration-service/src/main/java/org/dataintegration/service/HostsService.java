package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.DatabaseNotFoundException;
import org.dataintegration.exception.runtime.DuplicateHostException;
import org.dataintegration.jpa.entity.DatabaseEntity;
import org.dataintegration.jpa.entity.HostEntity;
import org.dataintegration.jpa.repository.JpaDatabaseRepository;
import org.dataintegration.jpa.repository.JpaHostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HostsService {

    private final JpaHostRepository jpaHostRepository;
    private final JpaDatabaseRepository jpaDatabaseRepository;

    public HostEntity createOrUpdate(HostEntity hostEntity) {
        checkDuplicateHostUrl(hostEntity);
        return jpaHostRepository.save(hostEntity);
    }

    public DatabaseEntity getDatabase(UUID databaseId) {
        return jpaDatabaseRepository.findById(databaseId)
                .orElseThrow(() -> new DatabaseNotFoundException("Database with id " + databaseId + " not found."));
    }

    public List<HostEntity> getAll() {
        return jpaHostRepository.findAll();
    }

    public void delete(UUID hostId) {
        jpaHostRepository.deleteById(hostId);
    }

    private void checkDuplicateHostUrl(HostEntity hostEntity) {
        final boolean urlExistsAlready;
        if (hostEntity.getId() == null) {
            urlExistsAlready = jpaHostRepository.existsByUrl(hostEntity.getUrl());
        } else {
            urlExistsAlready = jpaHostRepository.existsByUrlWithCount(hostEntity.getUrl(), hostEntity.getId());
        }
        if (urlExistsAlready) {
            throw new DuplicateHostException("Host url " + hostEntity.getUrl() + " already exists.");
        }
    }
}
