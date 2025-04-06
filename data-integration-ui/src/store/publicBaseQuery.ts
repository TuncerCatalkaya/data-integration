import BusySlice from "../features/busy/busy.slice"
import { BaseQueryFn, FetchArgs, fetchBaseQuery } from "@reduxjs/toolkit/query"
import { DataIntegrationExtraOptions } from "./store.types"
import i18next from "i18next"
import GetFrontendEnvironment from "../utils/GetFrontendEnvironment"

export const publicBaseQuery = (): BaseQueryFn<string | FetchArgs> => {
    return async (args, api, extraOptions: DataIntegrationExtraOptions) => {
        try {
            if (!extraOptions?.skipBusy) {
                api.dispatch(BusySlice.actions.setBusy(extraOptions?.busyText ? i18next.t(extraOptions.busyText) : undefined))
            }

            const baseQuery = fetchBaseQuery({
                baseUrl: GetFrontendEnvironment("VITE_REFRESH_TOKEN_ENDPOINT_BASE_URL"),
                credentials: "include"
            })

            return await baseQuery(args, api, extraOptions)
        } catch (e) {
            return { error: () => console.error("Error occurred", e) }
        } finally {
            if (!extraOptions?.skipBusy) {
                api.dispatch(BusySlice.actions.setIdle(extraOptions?.busyText ? i18next.t(extraOptions.busyText) : undefined))
            }
        }
    }
}
