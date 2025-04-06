package org.dataintegration.usecase.api;

import org.dataintegration.model.ProjectModel;
import org.dataintegration.usecase.model.CreateProjectsRequestModel;
import org.dataintegration.usecase.model.UpdateProjectsRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectsMethods {
    ProjectModel createNewProject(CreateProjectsRequestModel createProjectsRequest);

    ProjectModel updateProject(UpdateProjectsRequestModel updateProjectsRequest, String createdBy);

    void isProjectPermitted(UUID projectId, String createdBy);

    ProjectModel getProject(UUID projectId, String createdBy);

    Page<ProjectModel> getAllProjects(String createdBy, Pageable pageable);

    void markProjectForDeletion(UUID projectId, String createdBy);
}
