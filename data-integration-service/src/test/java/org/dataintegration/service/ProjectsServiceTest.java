package org.dataintegration.service;

import org.dataintegration.exception.runtime.ProjectForbiddenException;
import org.dataintegration.exception.runtime.ProjectNotFoundException;
import org.dataintegration.jpa.entity.ProjectEntity;
import org.dataintegration.jpa.repository.JpaProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectsServiceTest {

    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final Pageable PAGEABLE = PageRequest.of(2, 100);

    @Mock
    private JpaProjectRepository jpaProjectRepository;

    @InjectMocks
    private ProjectsService subject;

    @Mock
    private ProjectEntity projectEntity;
    @Mock
    private Page<ProjectEntity> projectEntityPage;

    @Test
    void testCreateOrUpdateProject() {
        when(jpaProjectRepository.save(projectEntity)).thenReturn(projectEntity);

        final ProjectEntity result = subject.createOrUpdateProject(projectEntity);

        assertThat(result).isEqualTo(projectEntity);
    }

    @Test
    void testGetProject() {
        when(jpaProjectRepository.findByIdAndCreatedByAndDeleteFalse(PROJECT_ID, "creator")).thenReturn(
                Optional.of(projectEntity));

        final ProjectEntity result = subject.getProject(PROJECT_ID, "creator");

        assertThat(result).isEqualTo(projectEntity);
    }

    @Test
    void testGetProjectProjectNotFoundException() {
        assertThatExceptionOfType(ProjectNotFoundException.class).isThrownBy(() -> subject.getProject(PROJECT_ID, "creator"));
    }

    @Test
    void testIsPermittedNull() {
        assertThatNoException().isThrownBy(() -> subject.isPermitted(null, "creator"));
    }

    @Test
    void testIsPermitted() {
        when(jpaProjectRepository.existsByIdAndCreatedByAndDeleteFalse(PROJECT_ID, "creator")).thenReturn(true);

        assertThatNoException().isThrownBy(() -> subject.isPermitted(PROJECT_ID, "creator"));
    }

    @Test
    void testIsPermittedProjectForbiddenException() {
        when(jpaProjectRepository.existsByIdAndCreatedByAndDeleteFalse(PROJECT_ID, "creator")).thenReturn(false);

        assertThatExceptionOfType(ProjectForbiddenException.class).isThrownBy(() -> subject.isPermitted(PROJECT_ID, "creator"));
    }

    @Test
    void testGetAll() {
        when(jpaProjectRepository.findAllByCreatedByAndDeleteFalse(eq("creator"), any(Pageable.class))).thenReturn(projectEntityPage);

        final Page<ProjectEntity> result = subject.getAll("creator", PAGEABLE);

        assertThat(result).isEqualTo(projectEntityPage);
    }

    @Test
    void testMarkForDeletion() {
        subject.markForDeletion(PROJECT_ID);
        verify(jpaProjectRepository).markForDeletion(PROJECT_ID);
    }

}