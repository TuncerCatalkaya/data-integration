export interface HostResponse {
    id: string
    name: string
    baseUrl: string
    integrationPath: string
    headerPath: string
    inUse: boolean
    databases: DatabaseResponse[]
}

export interface DatabaseResponse {
    id: string
    name: string
    inUse: boolean
}

export interface DataIntegrationHeaderAPIResponse {
    headers: DataIntegrationHeaderDataAPIResponse[]
}

export interface DataIntegrationHeaderDataAPIResponse {
    id: string
    display: string
    tooltip: string
    alternatives: string[]
}

export interface SelectedDatabaseResponse {
    id: string
    name: string
    host: SelectedHostResponse
}

export interface SelectedHostResponse {
    id: string
    name: string
    baseUrl: string
    integrationPath: string
    headerPath: string
}

export interface CreateOrUpdateHostsRequest {
    id: string
    name: string
    baseUrl: string
    integrationPath: string
    headerPath: string
    databases: CreateOrUpdateDatabasesRequest[]
}

export interface CreateOrUpdateDatabasesRequest {
    id: string
    name: string
}

export interface GetHostHeadersRequest {
    hostId: string
    language: string
}

export interface DeleteHostRequest {
    hostId: string
}
