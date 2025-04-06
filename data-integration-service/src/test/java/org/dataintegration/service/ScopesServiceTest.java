package org.dataintegration.service;

import org.dataintegration.exception.checked.ScopeHeaderValidationException;
import org.dataintegration.exception.runtime.ScopeNotFinishedException;
import org.dataintegration.exception.runtime.ScopeNotFoundException;
import org.dataintegration.jpa.entity.ProjectEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.jpa.repository.JpaScopeRepository;
import org.dataintegration.model.HeaderModel;
import org.dataintegration.model.cache.DataIntegrationCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScopesServiceTest {

    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID SCOPE_ID = UUID.randomUUID();

    @Mock
    private JpaScopeRepository jpaScopeRepository;
    @Mock
    private DataIntegrationCache dataIntegrationCache;

    @InjectMocks
    private ScopesService subject;

    @Mock
    private ProjectEntity projectEntity;
    @Mock
    private ScopeEntity scopeEntity;
    @Mock
    private List<ScopeEntity> scopeEntityList;

    @Test
    void testCreateOrGetScopeGet() {
        when(projectEntity.getId()).thenReturn(PROJECT_ID);
        when(jpaScopeRepository.findByProject_IdAndKeyAndDeleteFalse(PROJECT_ID, "scopeKey")).thenReturn(
                Optional.of(scopeEntity));

        final ScopeEntity result = subject.createOrGetScope(projectEntity, "scopeKey", false);

        assertThat(result).isEqualTo(scopeEntity);
    }

    @Test
    void testCreateOrGetScopeCreate() {
        when(projectEntity.getId()).thenReturn(PROJECT_ID);
        when(jpaScopeRepository.findByProject_IdAndKeyAndDeleteFalse(PROJECT_ID, "scopeKey")).thenReturn(Optional.empty());
        when(jpaScopeRepository.save(any(ScopeEntity.class))).thenReturn(scopeEntity);

        final ScopeEntity result = subject.createOrGetScope(projectEntity, "scopeKey", false);

        assertThat(result).isEqualTo(scopeEntity);
    }

    @Test
    void testGetAndCheckIfScopeFinished() {
        when(scopeEntity.isFinished()).thenReturn(true);
        when(jpaScopeRepository.findByIdAndDeleteFalse(SCOPE_ID)).thenReturn(Optional.of(scopeEntity));

        final ScopeEntity result = subject.getAndCheckIfScopeFinished(SCOPE_ID);

        assertThat(result).isEqualTo(scopeEntity);
    }

    @Test
    void testGetAndCheckIfScopeFinishedScopeNotFinishedException() {
        when(scopeEntity.isFinished()).thenReturn(false);
        when(jpaScopeRepository.findByIdAndDeleteFalse(SCOPE_ID)).thenReturn(Optional.of(scopeEntity));

        assertThatExceptionOfType(ScopeNotFinishedException.class).isThrownBy(() -> subject.getAndCheckIfScopeFinished(SCOPE_ID));
    }

    @Test
    void testGet() {
        when(jpaScopeRepository.findByIdAndDeleteFalse(SCOPE_ID)).thenReturn(Optional.of(scopeEntity));

        final ScopeEntity result = subject.get(SCOPE_ID);

        assertThat(result).isEqualTo(scopeEntity);
    }

    @Test
    void testGetScopeNotFoundException() {
        assertThatExceptionOfType(ScopeNotFoundException.class).isThrownBy(() -> subject.get(SCOPE_ID));
    }

    @Test
    void testGetByScopeKey() {
        when(jpaScopeRepository.findByProject_IdAndKeyAndDeleteFalse(PROJECT_ID, "scopeKey")).thenReturn(
                Optional.of(scopeEntity));

        final Optional<ScopeEntity> result = subject.get(PROJECT_ID, "scopeKey");

        assertThat(result).isEqualTo(Optional.of(scopeEntity));
    }

    @Test
    void testGetAll() {
        when(jpaScopeRepository.findAllByProject_idAndDeleteFalse(PROJECT_ID,
                Sort.by(Sort.Direction.ASC, "createdDate"))).thenReturn(scopeEntityList);

        final List<ScopeEntity> result = subject.getAll(PROJECT_ID);

        assertThat(result).isEqualTo(scopeEntityList);
    }

    @Test
    void testFinish() {
        subject.finish(SCOPE_ID);
        verify(jpaScopeRepository).finish(SCOPE_ID);
    }

    @Test
    void testUpdateHeadersEmptyScopeHeaders() {
        final Set<HeaderModel> headers = Set.of(
                HeaderModel.builder()
                        .id("header1")
                        .display("header1")
                        .build(),
                HeaderModel.builder()
                        .id("header2")
                        .display("header2")
                        .build(),
                HeaderModel.builder()
                        .id("header3")
                        .display("header3")
                        .build()
        );

        when(scopeEntity.getHeaders()).thenReturn(null).thenReturn(new LinkedHashSet<>());
        when(jpaScopeRepository.findByIdAndDeleteFalse(SCOPE_ID)).thenReturn(Optional.of(scopeEntity));

        final LinkedHashSet<HeaderModel> result = subject.updateHeaders(SCOPE_ID, headers);

        assertThat(result).isNotNull();
        verify(jpaScopeRepository).updateHeaders(eq(SCOPE_ID), anySet());
    }

    @Test
    void testUpdateHeadersWithScopeHeaders() {
        final LinkedHashSet<HeaderModel> scopeHeaders = new LinkedHashSet<>();
        scopeHeaders.add(HeaderModel.builder()
                .id("header1")
                .display("header1")
                .build());
        scopeHeaders.add(HeaderModel.builder()
                .id("header2")
                .display("header2")
                .build());
        scopeHeaders.add(HeaderModel.builder()
                .id("header3")
                .display("header3")
                .build());

        final Set<HeaderModel> headers = Set.of(
                HeaderModel.builder()
                        .id("header2")
                        .display("header2")
                        .build(),
                HeaderModel.builder()
                        .id("header1")
                        .display("newHeader1")
                        .build(),
                HeaderModel.builder()
                        .id("header3")
                        .display("newHeader3")
                        .build(),
                HeaderModel.builder()
                        .id("header4")
                        .display("header4")
                        .build()
        );

        when(scopeEntity.getHeaders()).thenReturn(scopeHeaders);
        when(jpaScopeRepository.findByIdAndDeleteFalse(SCOPE_ID)).thenReturn(Optional.of(scopeEntity));

        final LinkedHashSet<HeaderModel> result = subject.updateHeaders(SCOPE_ID, headers);

        assertThat(result).isNotNull();
        verify(jpaScopeRepository).updateHeaders(eq(SCOPE_ID), anySet());
    }

    @Test
    void testValidateHeaders() {
        final Set<HeaderModel> headers = Set.of(HeaderModel.builder()
                .id("header1")
                .display("header1")
                .build());

        assertThatNoException().isThrownBy(() -> subject.validateHeaders(headers));
    }

    @Test
    void testValidateHeadersEmptyHeader() {
        final Set<HeaderModel> headers = Set.of();
        assertThatExceptionOfType(ScopeHeaderValidationException.class).isThrownBy(() -> subject.validateHeaders(headers));
    }

    @Test
    void testValidateHeadersNoText() {
        final Set<HeaderModel> headers = Set.of(HeaderModel.builder()
                .id("")
                .display("header1")
                .build());
        assertThatExceptionOfType(ScopeHeaderValidationException.class).isThrownBy(() -> subject.validateHeaders(headers));
    }

    @Test
    void testMarkForDeletion() {
        subject.markForDeletion(SCOPE_ID);

        verify(jpaScopeRepository).markForDeletion(SCOPE_ID);
        verify(dataIntegrationCache).getMarkedForDeletionScopes();
    }

    @Test
    void testMarkForDeletionByProjectId() {
        final List<ScopeEntity> scopeEntities = List.of(new ScopeEntity());

        when(jpaScopeRepository.findAllByProject_idAndDeleteFalse(PROJECT_ID, Sort.unsorted())).thenReturn(scopeEntities);
        when(jpaScopeRepository.saveAll(scopeEntities)).thenReturn(scopeEntities);

        final List<ScopeEntity> result = subject.markForDeletionByProjectId(PROJECT_ID);

        assertThat(result).isEqualTo(scopeEntities)
                .first()
                .returns(true, ScopeEntity::isDelete);
    }

}