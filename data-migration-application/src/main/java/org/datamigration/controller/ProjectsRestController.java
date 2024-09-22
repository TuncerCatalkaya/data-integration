package org.datamigration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.datamigration.model.ItemModel;
import org.datamigration.model.ProjectModel;
import org.datamigration.model.ScopeModel;
import org.datamigration.usecase.CheckpointsUsecase;
import org.datamigration.usecase.ImportDataUsecase;
import org.datamigration.usecase.ProjectsUsecase;
import org.datamigration.usecase.model.CreateProjectsRequestModel;
import org.datamigration.usecase.model.CurrentCheckpointStatusResponseModel;
import org.datamigration.usecase.model.ImportDataResponseModel;
import org.datamigration.usecase.model.UpdateProjectsRequestModel;
import org.datamigration.utils.DataMigrationUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "/projects")
@RestController
@RequestMapping("${server.root.path}/projects")
@RequiredArgsConstructor
public class ProjectsRestController {

    private final ProjectsUsecase projectsUsecase;
    private final CheckpointsUsecase checkpointsUsecase;
    private final ImportDataUsecase importDataUsecase;

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping
    public ProjectModel createProject(@AuthenticationPrincipal Jwt jwt,
                                      @RequestBody CreateProjectsRequestModel createProjectsRequest) {
        return projectsUsecase.createNewProject(createProjectsRequest, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping(value = "/import-data-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportDataResponseModel importDataFile(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID projectId,
                                                  @RequestParam UUID scopeId, @RequestParam String delimiter,
                                                  @RequestParam MultipartFile file) {
        return importDataUsecase.importFromFile(file, projectId, scopeId, delimiter, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PostMapping("/import-data-s3")
    public ImportDataResponseModel importDataS3(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID scopeId,
                                                @RequestParam String bucket, @RequestParam String key,
                                                @RequestParam String delimiter) {
        return importDataUsecase.importFromS3(scopeId, bucket, key, delimiter, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PutMapping
    public ProjectModel updateProject(@AuthenticationPrincipal Jwt jwt,
                                      @RequestBody UpdateProjectsRequestModel updateProjectsRequest) {
        return projectsUsecase.updateProject(updateProjectsRequest, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PutMapping("/{projectId}/scopes")
    public ScopeModel createOrGetScope(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                       @RequestParam String scopeKey, @RequestParam boolean external) {
        return projectsUsecase.createOrGetScope(projectId, scopeKey, external, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}")
    public ProjectModel getProject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return projectsUsecase.getProject(projectId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping
    public Page<ProjectModel> getProjects(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        return projectsUsecase.getAllProjects(DataMigrationUtils.getJwtUserId(jwt), pageable);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}/scopes")
    public List<ScopeModel> getScopes(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return projectsUsecase.getAllScopes(projectId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}/scopes/{scopeId}/items")
    public Page<ItemModel> getItems(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID scopeId,
                                    @ParameterObject Pageable pageable) {
        return projectsUsecase.getAllItems(projectId, scopeId, DataMigrationUtils.getJwtUserId(jwt), pageable);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping("/{projectId}/scopes/{scopeId}/checkpoints/status")
    public CurrentCheckpointStatusResponseModel getCheckpointsStatus(@AuthenticationPrincipal Jwt jwt,
                                                                     @PathVariable UUID projectId, @PathVariable UUID scopeId) {
        return checkpointsUsecase.getCurrentCheckpointStatus(projectId, scopeId, DataMigrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @DeleteMapping("/{projectId}/scopes/{scopeId}")
    public void deleteScope(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID scopeId) {
        projectsUsecase.deleteScope(projectId, scopeId, DataMigrationUtils.getJwtUserId(jwt));
    }

}
