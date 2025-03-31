package org.dataintegration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.checked.ScopeHeaderValidationException;
import org.dataintegration.model.DataIntegrationAPIModel;
import org.dataintegration.model.HeaderModel;
import org.dataintegration.model.ItemModel;
import org.dataintegration.model.MappedItemModel;
import org.dataintegration.model.MappingModel;
import org.dataintegration.model.ProjectModel;
import org.dataintegration.model.ScopeModel;
import org.dataintegration.usecase.ImportDataUsecase;
import org.dataintegration.usecase.ProjectsUsecase;
import org.dataintegration.usecase.model.ApplyMappingRequestModel;
import org.dataintegration.usecase.model.ApplyUnmappingRequestModel;
import org.dataintegration.usecase.model.CreateOrUpdateMappingsRequestModel;
import org.dataintegration.usecase.model.CreateOrUpdateScopeHeadersRequestModel;
import org.dataintegration.usecase.model.CreateProjectsRequestModel;
import org.dataintegration.usecase.model.CurrentCheckpointStatusResponseModel;
import org.dataintegration.usecase.model.IntegrateRequestModel;
import org.dataintegration.usecase.model.UpdateItemPropertiesRequestModel;
import org.dataintegration.usecase.model.UpdateProjectsRequestModel;
import org.dataintegration.utils.DataIntegrationUtils;
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

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Tag(name = "/projects")
@RestController
@RequestMapping("${server.root.path}/projects")
@RequiredArgsConstructor
public class ProjectsRestController {

    private final ProjectsUsecase projectsUsecase;
    private final ImportDataUsecase importDataUsecase;

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PostMapping
    public ProjectModel createProject(@RequestBody CreateProjectsRequestModel createProjectsRequest) {
        return projectsUsecase.getProjectsMethods().createNewProject(createProjectsRequest);
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PostMapping(value = "/import-data-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importDataFile(@AuthenticationPrincipal Jwt jwt,
                               @RequestParam UUID projectId,
                               @RequestParam UUID scopeId,
                               @RequestParam String delimiter,
                               @RequestParam MultipartFile file) throws IOException {
        importDataUsecase.importFromFile(file.getBytes(), projectId, scopeId,
                DataIntegrationUtils.delimiterStringToCharMapper(delimiter), DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PostMapping("/import-data-s3")
    public void importDataS3(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID scopeId,
                             @RequestParam String bucket, @RequestParam String key) {
        importDataUsecase.importFromS3(scopeId, bucket, key, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PostMapping("/import-data-interrupt")
    public void interruptScope(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID projectId, @RequestParam UUID scopeId) {
        projectsUsecase.getScopesMethods().interruptScope(projectId, scopeId, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PostMapping("/{projectId}/mappings/apply-map")
    public void applyMapping(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                             @RequestBody ApplyMappingRequestModel applyMappingRequest) {
        projectsUsecase.getMappingsMethods()
                .applyMapping(projectId, applyMappingRequest, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PostMapping("/{projectId}/mapped-items/apply-unmap")
    public void applyUnmapping(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                               @RequestBody ApplyUnmappingRequestModel applyUnmappingRequest) {
        projectsUsecase.getMappedItemsMethods()
                .deleteMappedItems(projectId, applyUnmappingRequest, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PostMapping("/{projectId}/mappings/{mappingId}/mapped-items/integrate")
    public DataIntegrationAPIModel integrate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                             @PathVariable UUID mappingId, @RequestParam String language,
                                             @RequestBody IntegrateRequestModel integrateRequest) {
        return projectsUsecase.getMappedItemsMethods()
                .integrateMappedItems(projectId, mappingId, language, integrateRequest,
                        DataIntegrationUtils.getJwtUserId(jwt), jwt.getTokenValue());
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PutMapping
    public ProjectModel updateProject(@AuthenticationPrincipal Jwt jwt,
                                      @RequestBody UpdateProjectsRequestModel updateProjectsRequest) {
        return projectsUsecase.getProjectsMethods().updateProject(updateProjectsRequest, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PutMapping("/{projectId}/scopes")
    public ScopeModel createOrGetScope(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                       @RequestParam String scopeKey, @RequestParam boolean external) {
        return projectsUsecase.getScopesMethods()
                .createOrGetScope(projectId, scopeKey, external, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PutMapping("/{projectId}/scopes/{scopeId}/headers")
    public Set<HeaderModel> createOrUpdateScopeHeaders(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                                       @PathVariable UUID scopeId, @RequestBody
                                                       CreateOrUpdateScopeHeadersRequestModel createOrUpdateScopeHeadersRequest)
            throws ScopeHeaderValidationException {
        return projectsUsecase.getScopesMethods()
                .createOrUpdateScopeHeaders(projectId, scopeId, createOrUpdateScopeHeadersRequest,
                        DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PutMapping("/{projectId}/items/{itemId}/properties/{key}")
    public ItemModel updateItemProperty(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                        @PathVariable UUID itemId, @PathVariable String key, @RequestParam String newValue) {
        return projectsUsecase.getItemsMethods()
                .updateItemProperty(projectId, itemId, key, newValue, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PutMapping("/{projectId}/items/bulk/properties/{key}")
    public void updateItemProperties(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                     @RequestBody UpdateItemPropertiesRequestModel updateItemPropertiesRequest,
                                     @PathVariable String key, @RequestParam String newValue) {
        projectsUsecase.getItemsMethods().updateItemProperties(projectId, updateItemPropertiesRequest, key, newValue,
                DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PutMapping("/{projectId}/mapped-items/{mappedItemId}/properties/{key}")
    public MappedItemModel updateMappedItemProperty(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                                    @PathVariable UUID mappedItemId, @PathVariable String key,
                                                    @RequestParam(required = false) String newValue) {
        return projectsUsecase.getMappedItemsMethods()
                .updateMappedItemProperty(projectId, mappedItemId, key, newValue, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PutMapping("/{projectId}/scopes/{scopeId}/mappings")
    public MappingModel createOrUpdateMapping(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                              @PathVariable UUID scopeId,
                                              @RequestBody CreateOrUpdateMappingsRequestModel createMappingsRequest) {
        return projectsUsecase.getMappingsMethods()
                .createOrUpdateMapping(projectId, scopeId, createMappingsRequest, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @GetMapping("/{projectId}/permitted")
    public void isProjectPermitted(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        projectsUsecase.getProjectsMethods().isProjectPermitted(projectId, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @GetMapping("/{projectId}")
    public ProjectModel getProject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return projectsUsecase.getProjectsMethods().getProject(projectId, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @GetMapping
    public Page<ProjectModel> getProjects(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        return projectsUsecase.getProjectsMethods().getAllProjects(DataIntegrationUtils.getJwtUserId(jwt), pageable);
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @GetMapping("/{projectId}/scopes")
    public List<ScopeModel> getScopes(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return projectsUsecase.getScopesMethods().getAllScopes(projectId, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @GetMapping("/{projectId}/scopes/{scopeId}/headers")
    public Set<HeaderModel> getScopeHeaders(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                            @PathVariable UUID scopeId) {
        return projectsUsecase.getScopesMethods().getScopeHeaders(projectId, scopeId, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @GetMapping("/{projectId}/scopes/{scopeId}/items")
    public Page<ItemModel> getItems(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID scopeId,
                                    @RequestParam(required = false) UUID mappingId, @RequestParam boolean filterMappedItems,
                                    @RequestParam String header, @RequestParam String search,
                                    @ParameterObject Pageable pageable) {
        return projectsUsecase.getItemsMethods().getAllItems(projectId, scopeId, mappingId, filterMappedItems, header, search,
                DataIntegrationUtils.getJwtUserId(jwt), pageable);
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @GetMapping("/{projectId}/scopes/{scopeId}/mappings")
    public List<MappingModel> getMappings(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                          @PathVariable UUID scopeId) {
        return projectsUsecase.getMappingsMethods().getAllMappings(projectId, scopeId, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @GetMapping("/{projectId}/mappings/{mappingId}/mapped-items")
    public Page<MappedItemModel> getMappedItems(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                                @PathVariable UUID mappingId, @RequestParam boolean filterIntegratedItems,
                                                @ParameterObject Pageable pageable) {
        return projectsUsecase.getMappedItemsMethods()
                .getAllMappedItems(projectId, mappingId, filterIntegratedItems, DataIntegrationUtils.getJwtUserId(jwt), pageable);
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @GetMapping("/{projectId}/scopes/{scopeId}/checkpoints/status")
    public CurrentCheckpointStatusResponseModel getCheckpointsStatus(@AuthenticationPrincipal Jwt jwt,
                                                                     @PathVariable UUID projectId,
                                                                     @PathVariable UUID scopeId) {
        return projectsUsecase.getCheckpointsMethods()
                .getCurrentCheckpointStatus(projectId, scopeId, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @DeleteMapping("/{projectId}/mark")
    public void markProjectForDeletion(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        projectsUsecase.getProjectsMethods().markProjectForDeletion(projectId, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @DeleteMapping("/{projectId}/scopes/{scopeId}/mark")
    public void markScopeForDeletion(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID scopeId) {
        projectsUsecase.getScopesMethods().markScopeForDeletion(projectId, scopeId, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @DeleteMapping("/{projectId}/mappings/{mappingId}/mark")
    public void markMappingForDeletion(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                       @PathVariable UUID mappingId) {
        projectsUsecase.getMappingsMethods().markMappingForDeletion(projectId, mappingId, DataIntegrationUtils.getJwtUserId(jwt));
    }

}
