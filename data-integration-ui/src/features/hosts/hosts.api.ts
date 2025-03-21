import { createApi } from "@reduxjs/toolkit/query/react"
import { protectedBaseQuery } from "../../store/protectedBaseQuery"
import GetFrontendEnvironment from "../../utils/GetFrontendEnvironment"
import { CreateOrUpdateHostsRequest, DataIntegrationHeaderAPIResponse, DeleteHostRequest, GetHostHeadersRequest, HostResponse } from "./hosts.types"

const hostsUrl = "/hosts"

export const HostsApi = createApi({
    reducerPath: "hostsApi",
    baseQuery: protectedBaseQuery(),
    endpoints: builder => ({
        createOrUpdateHost: builder.mutation<HostResponse, CreateOrUpdateHostsRequest>({
            query: args => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + hostsUrl,
                method: "PUT",
                body: args
            })
        }),
        getHosts: builder.query<HostResponse[], void>({
            query: () => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + hostsUrl,
                method: "GET"
            })
        }),
        getHostHeaders: builder.query<DataIntegrationHeaderAPIResponse, GetHostHeadersRequest>({
            query: ({ hostId, language }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + hostsUrl + `/${hostId}/headers`,
                method: "GET",
                params: {
                    language
                }
            })
        }),
        deleteHost: builder.mutation<void, DeleteHostRequest>({
            query: ({ hostId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + hostsUrl + `/${hostId}`,
                method: "DELETE"
            })
        })
    })
})
