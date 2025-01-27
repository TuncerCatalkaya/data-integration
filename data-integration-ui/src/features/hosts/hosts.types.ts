export interface Host {
    id: string
    name: string
    url: string
    inUse: boolean
    databases: Database[]
}

export interface Database {
    id: string
    name: string
    inUse: boolean
}

export interface SelectedDatabase {
    id: string
    name: string
    host: SelectedHost
}

export interface SelectedHost {
    id: string
    name: string
    url: string
}

export interface CreateOrUpdateHostsRequest {
    id: string
    name: string
    url: string
    databases: CreateOrUpdateDatabasesRequest[]
}

export interface CreateOrUpdateDatabasesRequest {
    id: string
    name: string
}

export interface DeleteHostRequest {
    hostId: string
}
