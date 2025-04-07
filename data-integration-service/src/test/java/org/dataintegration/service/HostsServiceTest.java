package org.dataintegration.service;

import org.dataintegration.exception.runtime.DatabaseNotFoundException;
import org.dataintegration.exception.runtime.HostDomainValidationException;
import org.dataintegration.exception.runtime.HostNotFoundException;
import org.dataintegration.exception.runtime.HostValidationException;
import org.dataintegration.jpa.entity.DatabaseEntity;
import org.dataintegration.jpa.entity.HostEntity;
import org.dataintegration.jpa.repository.JpaDatabaseRepository;
import org.dataintegration.jpa.repository.JpaHostRepository;
import org.dataintegration.model.DataIntegrationHeaderDataAPIModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HostsServiceTest {

    private static final UUID HOST_ID = UUID.randomUUID();
    private static final UUID DATABASE_ID = UUID.randomUUID();

    @Mock
    private JpaHostRepository jpaHostRepository;
    @Mock
    private JpaDatabaseRepository jpaDatabaseRepository;
    @Mock
    private UrlService urlService;

    @InjectMocks
    private HostsService subject;

    @Mock
    private HostEntity hostEntity;
    @Mock
    private DatabaseEntity databaseEntity;
    @Mock
    private List<DataIntegrationHeaderDataAPIModel> headers;

    @Test
    void testCreateOrUpdateHost() {
        when(hostEntity.getDatabases()).thenReturn(Set.of(databaseEntity));
        when(hostEntity.getBaseUrl()).thenReturn("baseUrl");
        when(urlService.isDomainValid("baseUrl")).thenReturn(true);
        when(jpaHostRepository.save(hostEntity)).thenReturn(hostEntity);

        final HostEntity result = subject.createOrUpdateHost(hostEntity);

        assertThat(result).isEqualTo(hostEntity);
    }

    @Test
    void testCreateOrUpdateHostHostValidationException() {
        assertThatExceptionOfType(HostValidationException.class).isThrownBy(() -> subject.createOrUpdateHost(hostEntity));
    }

    @Test
    void testCreateOrUpdateHostHostDomainValidationException() {
        when(hostEntity.getDatabases()).thenReturn(Set.of(databaseEntity));
        when(hostEntity.getBaseUrl()).thenReturn("baseUrl");
        when(urlService.isDomainValid("baseUrl")).thenReturn(false);

        assertThatExceptionOfType(HostDomainValidationException.class).isThrownBy(() -> subject.createOrUpdateHost(hostEntity));
    }

    @Test
    void testUpdateHeadersByHostId() {
        subject.updateHeadersByHostId(HOST_ID, headers);
        verify(jpaHostRepository).updateHeadersByHostId(HOST_ID, headers);
    }

    @Test
    void testGetDatabase() {
        when(jpaDatabaseRepository.findById(DATABASE_ID)).thenReturn(Optional.of(databaseEntity));

        final DatabaseEntity result = subject.getDatabase(DATABASE_ID);

        assertThat(result).isEqualTo(databaseEntity);
    }

    @Test
    void testGetDatabaseDatabaseNotFoundException() {
        assertThatExceptionOfType(DatabaseNotFoundException.class).isThrownBy(() -> subject.getDatabase(DATABASE_ID));
    }

    @Test
    void testGetAllHosts() {
        final List<HostEntity> hosts = List.of(hostEntity);
        when(jpaHostRepository.findAll()).thenReturn(hosts);

        final List<HostEntity> result = subject.getAllHosts();

        assertThat(result).isEqualTo(hosts);
    }

    @Test
    void testGetHost() {
        when(jpaHostRepository.findById(HOST_ID)).thenReturn(Optional.of(hostEntity));

        final HostEntity result = subject.getHost(HOST_ID);

        assertThat(result).isEqualTo(hostEntity);
    }

    @Test
    void testGetHostHostNotFoundException() {
        assertThatExceptionOfType(HostNotFoundException.class).isThrownBy(() -> subject.getHost(HOST_ID));
    }

    @Test
    void testDeleteHost() {
        subject.deleteHost(HOST_ID);
        verify(jpaHostRepository).deleteById(HOST_ID);
    }

}