import { createSlice, PayloadAction } from "@reduxjs/toolkit"

interface ImportItemsScopePayload {
    projectId: string
    scope: string
}
interface ImportItemsMappingPayload {
    projectId: string
    mapping: string
}
interface ImportItemsFilterMappedItemPayload {
    projectId: string
    filterMappedItem: boolean
}

interface MappedItemsState {
    scopes: Record<string, string>
    mappings: Record<string, string>
    filterMappedItems: Record<string, boolean>
}
const initialState: MappedItemsState = {
    scopes: {},
    mappings: {},
    filterMappedItems: {}
}

const ImportItemsSlice = createSlice({
    name: "importItemsSlice",
    initialState,
    reducers: {
        putScope: (state, action: PayloadAction<ImportItemsScopePayload>) => {
            state.scopes[action.payload.projectId] = action.payload.scope
        },
        putMapping: (state, action: PayloadAction<ImportItemsMappingPayload>) => {
            state.mappings[action.payload.projectId] = action.payload.mapping
        },
        putFilterMappedItem: (state, action: PayloadAction<ImportItemsFilterMappedItemPayload>) => {
            state.filterMappedItems[action.payload.projectId] = action.payload.filterMappedItem
        }
    }
})

export default ImportItemsSlice
