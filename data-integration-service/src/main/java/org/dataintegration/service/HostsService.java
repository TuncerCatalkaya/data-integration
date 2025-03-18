package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.DatabaseNotFoundException;
import org.dataintegration.exception.runtime.HostDomainNotValidException;
import org.dataintegration.exception.runtime.HostNotValidException;
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
    private final UrlService urlService;

    public HostEntity createOrUpdate(HostEntity hostEntity) {
        if (hostEntity.getDatabases().isEmpty()) {
            throw new HostNotValidException();
        }
        hostEntity.getDatabases().forEach(databaseEntity -> databaseEntity.setHost(hostEntity));
        if (!urlService.isDomainValid(hostEntity.getBaseUrl())) {
            throw new HostDomainNotValidException();
        }
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

}
