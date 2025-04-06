package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.ProjectForbiddenException;
import org.dataintegration.exception.runtime.ProjectNotFoundException;
import org.dataintegration.jpa.entity.ProjectEntity;
import org.dataintegration.jpa.repository.JpaProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for projects.
 */
@Service
@RequiredArgsConstructor
public class ProjectsService {

    private final JpaProjectRepository jpaProjectRepository;

    /**
     * Create or update a project.
     *
     * @param projectEntity {@link ProjectEntity}
     * @return created or update {@link ProjectEntity}
     */
    public ProjectEntity createOrUpdateProject(ProjectEntity projectEntity) {
        return jpaProjectRepository.save(projectEntity);
    }

    /**
     * Get project by project id and creator.
     *
     * @param projectId project id
     * @param createdBy creator
     * @return {@link ProjectEntity}
     * @throws ProjectNotFoundException in case project entity is not found in database
     */
    public ProjectEntity getProject(UUID projectId, String createdBy) {
        return jpaProjectRepository.findByIdAndCreatedByAndDeleteFalse(projectId, createdBy)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + projectId + " not found."));
    }

    /**
     * Checks if a user is permitted to have access to that project by checking the creator.
     *
     * @param projectId project id
     * @param createdBy creator
     * @throws ProjectForbiddenException in case user is forbidden to access that project
     */
    public void isPermitted(UUID projectId, String createdBy) throws ProjectForbiddenException {
        if (projectId == null) {
            return;
        }
        final boolean isPermitted = jpaProjectRepository.existsByIdAndCreatedByAndDeleteFalse(projectId, createdBy);
        if (!isPermitted) {
            throw new ProjectForbiddenException("Forbidden to access project with id " + projectId + ".");
        }
    }

    /**
     * Get all projects paginated that created by the creator.
     *
     * @param createdBy creator
     * @param pageable {@link Pageable}
     * @return {@link Page} of {@link ProjectEntity}
     */
    public Page<ProjectEntity> getAll(String createdBy, Pageable pageable) {
        final Pageable pageRequest =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSortOr(
                        Sort.by(Sort.Direction.DESC, "lastModifiedDate")));
        return jpaProjectRepository.findAllByCreatedByAndDeleteFalse(createdBy, pageRequest);
    }

    /**
     * Mark project for deletion by project id.
     *
     * @param projectId project id
     */
    public void markForDeletion(UUID projectId) {
        jpaProjectRepository.markForDeletion(projectId);
    }

}
