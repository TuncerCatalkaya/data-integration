import { createSlice, PayloadAction } from "@reduxjs/toolkit"

interface MappedItemsScopePayload {
    projectId: string
    scope: string
}
interface MappedItemsMappingPayload {
    projectId: string
    mapping: string
}
interface MappedItemsFilterIntegratedItemPayload {
    projectId: string
    filterIntegratedItem: boolean
}

interface MappedItemsState {
    scopes: Record<string, string>
    mappings: Record<string, string>
    filterIntegratedItems: Record<string, boolean>
}
const initialState: MappedItemsState = {
    scopes: {},
    mappings: {},
    filterIntegratedItems: {}
}

const MappedItemsSlice = createSlice({
    name: "mappedItemsSlice",
    initialState,
    reducers: {
        putScope: (state, action: PayloadAction<MappedItemsScopePayload>) => {
            state.scopes[action.payload.projectId] = action.payload.scope
        },
        putMapping: (state, action: PayloadAction<MappedItemsMappingPayload>) => {
            state.mappings[action.payload.projectId] = action.payload.mapping
        },
        putFilterIntegratedItem: (state, action: PayloadAction<MappedItemsFilterIntegratedItemPayload>) => {
            state.filterIntegratedItems[action.payload.projectId] = action.payload.filterIntegratedItem
        }
    }
})

export default MappedItemsSlice
