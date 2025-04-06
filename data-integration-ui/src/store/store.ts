import { configureStore, Store } from "@reduxjs/toolkit"
import BusySlice from "../features/busy/busy.slice"
import AuthSlice from "../features/auth/auth.slice"
import { Dispatch, RootState } from "./store.types"
import { TypedUseSelectorHook, useDispatch, useSelector } from "react-redux"
import { AuthApi } from "../features/auth/auth.api"
import { ProjectsApi } from "../features/projects/projects.api"
import { S3Api } from "../features/s3/s3.api"
import { HostsApi } from "../features/hosts/hosts.api"
import MappedItemsSlice from "../features/mappedItems/mappedItems.slice"
import ImportItemsSlice from "../features/importItems/importItems.slice"

export const store: Store = configureStore({
    reducer: {
        busy: BusySlice.reducer,
        auth: AuthSlice.reducer,
        importItems: ImportItemsSlice.reducer,
        mappedItems: MappedItemsSlice.reducer,
        [AuthApi.reducerPath]: AuthApi.reducer,
        [ProjectsApi.reducerPath]: ProjectsApi.reducer,
        [HostsApi.reducerPath]: HostsApi.reducer,
        [S3Api.reducerPath]: S3Api.reducer
    },
    middleware: getDefaultMiddleware =>
        getDefaultMiddleware({ serializableCheck: false })
            .concat(AuthApi.middleware)
            .concat(ProjectsApi.middleware)
            .concat(HostsApi.middleware)
            .concat(S3Api.middleware)
})

export const useAppDispatch: () => Dispatch = useDispatch
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
