package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.DatabaseNotFoundException;
import org.dataintegration.exception.runtime.HostDomainValidationException;
import org.dataintegration.exception.runtime.HostNotFoundException;
import org.dataintegration.exception.runtime.HostValidationException;
import org.dataintegration.jpa.entity.DatabaseEntity;
import org.dataintegration.jpa.entity.HostEntity;
import org.dataintegration.jpa.repository.JpaDatabaseRepository;
import org.dataintegration.jpa.repository.JpaHostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for host and database.
 */
@Service
@RequiredArgsConstructor
public class HostsService {

    private final JpaHostRepository jpaHostRepository;
    private final JpaDatabaseRepository jpaDatabaseRepository;
    private final UrlService urlService;

    /**
     * Create or update host.
     *
     * @param hostEntity {@link HostEntity}
     * @return create or updated {@link HostEntity}
     * @throws HostValidationException in case {@link HostEntity} that is used to create or update is invalid
     * @throws HostDomainValidationException in case {@link HostEntity} base url has invalid domain
     */
    public HostEntity createOrUpdateHost(HostEntity hostEntity) throws HostValidationException, HostDomainValidationException {
        if (hostEntity.getDatabases().isEmpty()) {
            throw new HostValidationException();
        }
        hostEntity.getDatabases().forEach(databaseEntity -> databaseEntity.setHost(hostEntity));
        if (!urlService.isDomainValid(hostEntity.getBaseUrl())) {
            throw new HostDomainValidationException();
        }
        return jpaHostRepository.save(hostEntity);
    }

    /**
     * Get database by database id.
     *
     * @param databaseId database id
     * @return {@link DatabaseEntity}
     * @throws DatabaseNotFoundException in case database entity is not found in database
     */
    public DatabaseEntity getDatabase(UUID databaseId) throws DatabaseNotFoundException {
        return jpaDatabaseRepository.findById(databaseId)
                .orElseThrow(() -> new DatabaseNotFoundException("Database with id " + databaseId + " not found."));
    }

    /**
     * Get all hosts.
     *
     * @return {@link List} of {@link HostEntity}
     */
    public List<HostEntity> getAllHosts() {
        return jpaHostRepository.findAll();
    }

    /**
     * Get host by host id.
     *
     * @param hostId host id
     * @return {@link HostEntity}
     * @throws HostNotFoundException in case host entity is not found in database
     */
    public HostEntity getHost(UUID hostId) throws HostNotFoundException {
        return jpaHostRepository.findById(hostId)
                .orElseThrow(() -> new HostNotFoundException("Host with id " + hostId + " not found."));
    }

    /**
     * Delete host by host id.
     *
     * @param hostId host id
     */
    public void deleteHost(UUID hostId) {
        jpaHostRepository.deleteById(hostId);
    }

}
