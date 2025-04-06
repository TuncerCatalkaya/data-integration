import { createApi } from "@reduxjs/toolkit/query/react"
import { protectedBaseQuery } from "../../store/protectedBaseQuery"
import GetFrontendEnvironment from "../../utils/GetFrontendEnvironment"
import {
    AbortMultipartUploadRequest,
    CompleteMultipartUploadRequest,
    DeleteObjectRequest,
    GeneratePresignedUrlResponse,
    GeneratePresignedUrlsMultiPartUploadRequest,
    InitiateMultipartUploadRequest,
    InitiateMultipartUploadResponse,
    S3ListRequest,
    S3ListResponse
} from "./s3.types"

const s3Url = "/s3"

export const S3Api = createApi({
    reducerPath: "s3Api",
    baseQuery: protectedBaseQuery(),
    endpoints: builder => ({
        initiateMultipartUpload: builder.mutation<InitiateMultipartUploadResponse, InitiateMultipartUploadRequest>({
            query: ({ bucket, key }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + s3Url + "/multipart-upload/initiate",
                method: "POST",
                params: {
                    bucket,
                    key
                }
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        completeMultipartUpload: builder.mutation<void, CompleteMultipartUploadRequest>({
            query: ({ bucket, key, uploadId, lineCount, delimiter, completedParts }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + s3Url + "/multipart-upload/complete",
                method: "POST",
                body: completedParts,
                params: {
                    bucket,
                    key,
                    uploadId,
                    lineCount,
                    delimiter
                }
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        abortMultipartUpload: builder.mutation<void, AbortMultipartUploadRequest>({
            query: ({ bucket, key, uploadId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + s3Url + "/multipart-upload/abort",
                method: "POST",
                params: {
                    bucket,
                    key,
                    uploadId
                }
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        generatePresignedUrlMultiPartUpload: builder.mutation<GeneratePresignedUrlResponse, GeneratePresignedUrlsMultiPartUploadRequest>({
            query: ({ bucket, key, uploadId, partNumber }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + s3Url + "/multipart-upload/presigned-url",
                method: "GET",
                params: {
                    bucket,
                    key,
                    uploadId,
                    partNumber
                }
            }),
            extraOptions: {
                skipBusy: true
            }
        }),
        listObjectsV2: builder.mutation<S3ListResponse[], S3ListRequest>({
            query: ({ bucket, projectId }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + s3Url + "/objects",
                method: "GET",
                params: {
                    bucket,
                    projectId
                }
            })
        }),
        deleteObject: builder.mutation<void, DeleteObjectRequest>({
            query: ({ bucket, key }) => ({
                url: GetFrontendEnvironment("VITE_BASE_URL_ROOT_PATH") + s3Url + "/objects",
                method: "DELETE",
                params: {
                    bucket,
                    key
                }
            })
        })
    })
})
