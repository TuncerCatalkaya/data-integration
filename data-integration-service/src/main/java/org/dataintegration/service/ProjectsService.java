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

@Service
@RequiredArgsConstructor
public class ProjectsService {

    private final JpaProjectRepository jpaProjectRepository;

    public ProjectEntity createOrUpdateProject(ProjectEntity projectEntity) {
        return jpaProjectRepository.save(projectEntity);
    }

    public ProjectEntity getProject(UUID projectId, String createdBy) {
        return jpaProjectRepository.findByIdAndCreatedByAndDeleteFalse(projectId, createdBy)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + projectId + " not found."));
    }

    public void isPermitted(UUID projectId, String createdBy) {
        if (projectId == null) {
            return;
        }
        final boolean isPermitted = jpaProjectRepository.existsByIdAndCreatedByAndDeleteFalse(projectId, createdBy);
        if (!isPermitted) {
            throw new ProjectForbiddenException("Forbidden to access project with id " + projectId + ".");
        }
    }

    public Page<ProjectEntity> getAll(String createdBy, Pageable pageable) {
        final Pageable pageRequest =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSortOr(
                        Sort.by(Sort.Direction.DESC, "lastModifiedDate")));
        return jpaProjectRepository.findAllByCreatedByAndDeleteFalse(createdBy, pageRequest);
    }

    public void markForDeletion(UUID projectId) {
        jpaProjectRepository.markForDeletion(projectId);
    }

}
