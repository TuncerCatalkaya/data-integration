import { createApi } from "@reduxjs/toolkit/query/react"
import { publicBaseQuery } from "../../store/publicBaseQuery"
import GetFrontendEnvironment from "../../utils/GetFrontendEnvironment"

export const AuthApi = createApi({
    reducerPath: "authApi",
    baseQuery: publicBaseQuery(),
    endpoints: builder => ({
        refreshToken: builder.mutation<Record<string, string>, void>({
            query: () => ({
                url: GetFrontendEnvironment("VITE_REFRESH_TOKEN_ENDPOINT_URL"),
                method: GetFrontendEnvironment("VITE_REFRESH_TOKEN_ENDPOINT_METHOD")
            }),
            extraOptions: {
                skipBusy: true
            }
        })
    })
})
