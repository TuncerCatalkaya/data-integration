export interface MappingsInput {
    id: string
    header: string
    values: MappingsValuesInput[]
}

export interface MappingsValuesInput {
    id: string
    value: string
}

export interface SourceInput {
    id: string
    value: string
}
