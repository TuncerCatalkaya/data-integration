export interface CompletedPart {
    etag: string
    partNumber: number
}

export interface InitiateMultipartUploadRequest {
    bucket: string
    key: string
}

export interface CompleteMultipartUploadRequest {
    bucket: string
    key: string
    uploadId: string
    lineCount: number
    delimiter: string
    completedParts: CompletedPart[]
}

export interface AbortMultipartUploadRequest {
    bucket: string
    key: string
    uploadId: string
}

export interface GeneratePresignedUrlsMultiPartUploadRequest {
    bucket: string
    key: string
    uploadId: string
    partNumber: number
}

export interface S3ListRequest {
    bucket: string
    projectId: string
}

export interface DeleteObjectRequest {
    bucket: string
    key: string
}

export interface InitiateMultipartUploadResponse {
    uploadId: string
}

export interface GeneratePresignedUrlResponse {
    presignedUrl: string
}

export interface S3ListResponse {
    key: string
    lastModified: Date
    size: number
    checkpoint: boolean
}
