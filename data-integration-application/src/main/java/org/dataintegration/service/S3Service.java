package org.dataintegration.service;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.BucketNotFoundException;
import org.dataintegration.exception.KeyNotFoundException;
import org.dataintegration.exception.TagNotFoundException;
import org.dataintegration.model.CompletedPartModel;
import org.dataintegration.model.DelimiterModel;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public String initiateMultipartUpload(String bucket, String key) {
        try {
            final CreateMultipartUploadRequest createMultipartUploadPresignRequest = CreateMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            final CreateMultipartUploadResponse createMultipartUploadResponse =
                    s3Client.createMultipartUpload(createMultipartUploadPresignRequest);

            return createMultipartUploadResponse.uploadId();
        } catch (NoSuchBucketException ex) {
            throw new BucketNotFoundException(bucket);
        } catch (NoSuchKeyException ex) {
            throw new KeyNotFoundException(key);
        }
    }

    public String generatePresignedUrlMultiPartUpload(String bucket, String key, String uploadId, int partNumber) {
        try {
            final UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                    .uploadPartRequest(uploadPartRequestBuilder -> uploadPartRequestBuilder
                            .bucket(bucket)
                            .key(key)
                            .uploadId(uploadId)
                            .partNumber(partNumber))
                    .signatureDuration(Duration.ofHours(1))
                    .build();

            PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(presignRequest);
            return presignedRequest.url().toString();
        } catch (NoSuchBucketException ex) {
            throw new BucketNotFoundException(bucket);
        } catch (NoSuchKeyException ex) {
            throw new KeyNotFoundException(key);
        }
    }

    @SuppressWarnings("checkstyle:MethodLength")
    public void completeMultipartUpload(String bucket, String key, String uploadId, long lineCount, char delimiter,
                                        List<CompletedPartModel> completedParts) {
        try {
            final CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(multiPartUploadBuilder -> multiPartUploadBuilder
                            .parts(completedParts.stream()
                                    .map(completedPart -> CompletedPart.builder()
                                            .eTag(completedPart.getETag())
                                            .partNumber(completedPart.getPartNumber())
                                            .build())
                                    .toList()
                            )
                    )
                    .build();

            s3Client.completeMultipartUpload(completeMultipartUploadRequest);

            final PutObjectTaggingRequest putObjectTaggingRequest = PutObjectTaggingRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .tagging(tagginBuilder -> tagginBuilder
                            .tagSet(
                                    Tag.builder()
                                            .key("lineCount")
                                            .value(String.valueOf(lineCount))
                                            .build(),
                                    Tag.builder()
                                            .key("delimiter")
                                            .value(DelimiterModel.fromCharacter(delimiter).toString())
                                            .build()
                            )
                    )
                    .build();
            s3Client.putObjectTagging(putObjectTaggingRequest);
        } catch (NoSuchBucketException ex) {
            throw new BucketNotFoundException(bucket);
        } catch (NoSuchKeyException ex) {
            throw new KeyNotFoundException(key);
        }
    }

    public void abortMultipartUpload(String bucket, String key, String uploadId) {
        try {
            final AbortMultipartUploadRequest abortMultipartUploadRequest = AbortMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .build();
            s3Client.abortMultipartUpload(abortMultipartUploadRequest);
        } catch (NoSuchBucketException ex) {
            throw new BucketNotFoundException(bucket);
        } catch (NoSuchKeyException ex) {
            throw new KeyNotFoundException(key);
        }
    }

    public ResponseInputStream<GetObjectResponse> getS3Object(String bucket, String key) {
        try {
            final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            return s3Client.getObject(getObjectRequest);
        } catch (NoSuchBucketException ex) {
            throw new BucketNotFoundException(bucket);
        } catch (NoSuchKeyException ex) {
            throw new KeyNotFoundException(key);
        }
    }

    public String getS3ObjectTag(String bucket, String key, String tag) {
        try {
            final GetObjectTaggingRequest getObjectTaggingRequest = GetObjectTaggingRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            return s3Client.getObjectTagging(getObjectTaggingRequest).tagSet().stream()
                    .filter(t -> t.key().equals(tag))
                    .findFirst()
                    .orElseThrow(() -> new TagNotFoundException("Tag " + tag + " not found for " + bucket + "/" + key + "."))
                    .value();
        } catch (NoSuchBucketException ex) {
            throw new BucketNotFoundException(bucket);
        } catch (NoSuchKeyException ex) {
            throw new KeyNotFoundException(key);
        }
    }

    public ListObjectsV2Response listObjectsV2(String bucket, String projectId) {
        try {
            final ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(projectId + "/")
                    .build();
            return s3Client.listObjectsV2(listObjectsV2Request);
        } catch (NoSuchBucketException ex) {
            throw new BucketNotFoundException(bucket);
        }
    }

    public void deleteObject(String bucket, String key) {
        try {
            final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (NoSuchBucketException ex) {
            throw new BucketNotFoundException(bucket);
        } catch (NoSuchKeyException ex) {
            throw new KeyNotFoundException(key);
        }
    }
}
