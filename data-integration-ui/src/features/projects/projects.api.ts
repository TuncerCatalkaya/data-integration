import {createApi} from "@reduxjs/toolkit/query/react"
import {protectedBaseQuery} from "../../store/protectedBaseQuery"
import {
    ApplyMappingRequest,
    ApplyUnmappingRequest,
    CreateOrGetScopeRequest,
    CreateOrUpdateMappingsRequest,
    CreateOrUpdateScopeHeadersRequest,
    CreateProjectRequest,
    GetCurrentCheckpointStatusRequest,
    GetCurrentCheckpointStatusResponse,
    GetItemsRequest,
    GetItemsResponse,
    GetMappedItemsByMappingResponse,
    GetMappedItemsRequest,
    GetMappingsRequest,
    GetProjectRequest,
    GetProjectsRequest,
    GetProjectsResponse,
    GetScopeHeadersRequest,
    GetScopesRequest,
    ImportDataFileRequest,
    ImportDataS3Request,
    InterruptScopeRequest,
    IsProjectPermittedRequest,
    ItemResponse,
    MappedItemResponse,
    MappingResponse,
    MarkMappingForDeletionRequest,
    MarkProjectForDeletionRequest,
    MarkScopeForDeletionRequest,
    ProjectResponse,
    ScopeHeaderResponse,
    ScopeResponse,
    UpdateItemPropertiesRequest,
    UpdateItemPropertyRequest,
    UpdateMappedItemPropertyRequest,
    UpdateProjectRequest
} from "./projects.types"
import GetFrontendEnvironment from "../../utils/GetFrontendEnvironment"

const projectsUrl = "/projects"

export const ProjectsApi = createApi({
    reducerPath: "projectsApi",
    baseQuery: protectedBaseQuery(),
    endpoints: builder => ({
        createProject: builder.mutation<ProjectResponse, CreateProjectRequest>({
            query: ({ projectName }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl,
                method: "POST",
                body: {
                    projectName
                }
            })
        }),
        importDataFile: builder.mutation<void, ImportDataFileRequest>({
            query: args => {
                const formData = new FormData()
                formData.append("projectId", args.projectId)
                formData.append("scopeId", args.scopeId)
                formData.append("delimiter", args.delimiter)
                formData.append("file", args.file)
                return {
                    url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + "/import-data-file",
                    method: "POST",
                    body: formData
                }
            },
            extraOptions: {
                skipBusy: true
            }
        }),
        importDataS3: builder.mutation<void, ImportDataS3Request>({
            query: args => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + "/import-data-s3",
                method: "POST",
                params: args
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        interruptScope: builder.mutation<void, InterruptScopeRequest>({
            query: args => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + "/import-data-interrupt",
                method: "POST",
                params: args
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        applyMapping: builder.mutation<void, ApplyMappingRequest>({
            query: ({ projectId, mappingId, itemIds }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/mappings/apply-map`,
                method: "POST",
                body: {
                    mappingId,
                    itemIds
                }
            })
        }),
        applyUnmapping: builder.mutation<void, ApplyUnmappingRequest>({
            query: ({ projectId, mappedItemIds }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/mapped-items/apply-unmap`,
                method: "POST",
                body: {
                    mappedItemIds
                }
            })
        }),
        updateProject: builder.mutation<ProjectResponse, UpdateProjectRequest>({
            query: ({ projectId, projectName }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl,
                method: "PUT",
                body: {
                    projectId,
                    projectName
                }
            })
        }),
        createOrGetScope: builder.mutation<ScopeResponse, CreateOrGetScopeRequest>({
            query: ({ projectId, scopeKey, external }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes`,
                method: "PUT",
                params: {
                    scopeKey,
                    external
                }
            })
        }),
        createOrUpdateScopeHeaders: builder.mutation<ScopeHeaderResponse[], CreateOrUpdateScopeHeadersRequest>({
            query: ({ projectId, scopeId, headers }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/headers`,
                method: "PUT",
                body: {
                    headers
                }
            })
        }),
        updateItemProperty: builder.mutation<ItemResponse, UpdateItemPropertyRequest>({
            query: ({ projectId, itemId, key, newValue }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/items/${itemId}/properties/${key}`,
                method: "PUT",
                params: {
                    newValue
                }
            })
        }),
        updateItemProperties: builder.mutation<void, UpdateItemPropertiesRequest>({
            query: ({ projectId, itemIds, key, newValue }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/items/bulk/properties/${key}`,
                method: "PUT",
                params: {
                    newValue
                },
                body: {
                    itemIds
                }
            })
        }),
        updateMappedItemProperty: builder.mutation<MappedItemResponse, UpdateMappedItemPropertyRequest>({
            query: ({ projectId, mappedItemId, key, newValue }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/mapped-items/${mappedItemId}/properties/${key}`,
                method: "PUT",
                params: {
                    newValue
                }
            })
        }),
        createOrUpdateMapping: builder.mutation<MappingResponse, CreateOrUpdateMappingsRequest>({
            query: ({ projectId, scopeId, mappingId, databaseId, mappingName, mapping }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/mappings`,
                method: "PUT",
                body: {
                    mappingId,
                    databaseId,
                    mappingName,
                    mapping
                }
            })
        }),
        isProjectPermitted: builder.query<void, IsProjectPermittedRequest>({
            query: ({ projectId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/permitted`,
                method: "GET"
            })
        }),
        getProject: builder.query<ProjectResponse, GetProjectRequest>({
            query: ({ projectId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}`,
                method: "GET"
            })
        }),
        getProjects: builder.query<GetProjectsResponse, GetProjectsRequest>({
            query: args => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl,
                method: "GET",
                params: args
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        getScopes: builder.query<ScopeResponse[], GetScopesRequest>({
            query: ({ projectId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes`,
                method: "GET"
            })
        }),
        getScopeHeaders: builder.query<ScopeHeaderResponse[], GetScopeHeadersRequest>({
            query: ({ projectId, scopeId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/headers`,
                method: "GET"
            })
        }),
        getItems: builder.query<GetItemsResponse, GetItemsRequest>({
            query: ({ projectId, scopeId, mappingId, filterMappedItems, header, search, page, size, sort }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/items`,
                method: "GET",
                params: {
                    mappingId,
                    filterMappedItems,
                    header,
                    search,
                    page,
                    size,
                    sort
                }
            })
        }),
        getMappings: builder.query<MappingResponse[], GetMappingsRequest>({
            query: ({ projectId, scopeId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/mappings`,
                method: "GET"
            })
        }),
        getMappedItems: builder.query<GetMappedItemsByMappingResponse, GetMappedItemsRequest>({
            query: ({ projectId, mappingId, page, size, sort }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/mappings/${mappingId}/mapped-items`,
                method: "GET",
                params: {
                    page,
                    size,
                    sort
                }
            })
        }),
        getCurrentCheckpointStatus: builder.query<GetCurrentCheckpointStatusResponse, GetCurrentCheckpointStatusRequest>({
            query: ({ projectId, scopeId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/checkpoints/status`,
                method: "GET"
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        markProjectForDeletion: builder.mutation<void, MarkProjectForDeletionRequest>({
            query: ({ projectId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/mark`,
                method: "DELETE"
            })
        }),
        markScopeForDeletion: builder.mutation<void, MarkScopeForDeletionRequest>({
            query: ({ projectId, scopeId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/scopes/${scopeId}/mark`,
                method: "DELETE"
            })
        }),
        markMappingForDeletion: builder.mutation<void, MarkMappingForDeletionRequest>({
            query: ({ projectId, mappingId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + projectsUrl + `/${projectId}/mappings/${mappingId}/mark`,
                method: "DELETE"
            })
        })
    })
})
