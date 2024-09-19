package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.exception.TagNotFoundException;
import org.datamigration.model.CompletedPartModel;
import org.datamigration.service.S3Service;
import org.datamigration.usecase.model.GeneratePresignedUrlResponseModel;
import org.datamigration.usecase.model.InitiateMultipartUploadRequestModel;
import org.datamigration.usecase.model.S3ListResponseModel;
import org.datamigration.utils.DataMigrationUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Usecase {

    private final S3Service s3Service;
    private final ProjectsUsecase projectsUsecase;

    public InitiateMultipartUploadRequestModel initiateMultipartUpload(String bucket, String key, String owner) {
        isPermitted(key, owner);
        return InitiateMultipartUploadRequestModel.builder()
                .uploadId(s3Service.initiateMultipartUpload(bucket, key))
                .build();
    }

    public GeneratePresignedUrlResponseModel generatePresignedUrlMultiPartUpload(String bucket, String key, String uploadId, int partNumber, String owner) {
        isPermitted(key, owner);
        return GeneratePresignedUrlResponseModel.builder()
                .presignedUrl(s3Service.generatePresignedUrlMultiPartUpload(bucket, key, uploadId, partNumber))
                .build();
    }

    public void completeMultipartUpload(String bucket, String key, String uploadId, long lineCount, List<CompletedPartModel> completedParts, String owner) {
        isPermitted(key, owner);
        s3Service.completeMultipartUpload(bucket, key, uploadId, lineCount, completedParts);
    }

    public void abortMultipartUpload(String bucket, String key, String uploadId, String owner) {
        isPermitted(key, owner);
        s3Service.abortMultipartUpload(bucket, key, uploadId);
    }

    public ResponseInputStream<GetObjectResponse> getObject(String bucket, String key, String owner) {
        isPermitted(key, owner);
        return s3Service.getS3Object(bucket, key);
    }

    public String getObjectTag(String bucket, String key, String owner, String tag) {
        isPermitted(key, owner);
        final GetObjectTaggingResponse tags = s3Service.getS3ObjectTags(bucket, key);
        return tags.tagSet().stream()
                .filter(t -> t.key().equals(tag))
                .findFirst()
                .orElseThrow(() -> new TagNotFoundException("Tag " + tag + " not found for " + bucket + "/" + key + "."))
                .value();
    }

    public List<S3ListResponseModel> listObjectsV2(String bucket, String projectId, String owner) {
        isPermitted(projectId, owner);
        final ListObjectsV2Response listObjectsV2Response = s3Service.listObjectsV2(bucket, projectId);
        return listObjectsV2Response.contents().stream()
                .map(s3Object ->  S3ListResponseModel.builder()
                        .key(s3Object.key())
                        .lastModified(Date.from(s3Object.lastModified()))
                        .size(s3Object.size())
                        .build())
                .toList();
    }

    public void deleteObject(String bucket, String key, String owner) {
        isPermitted(key, owner);
        s3Service.deleteObject(bucket, key);
    }

    public void isPermitted(String key, String owner) {
        final UUID projectId = DataMigrationUtils.getProjectIdFromS3Key(key);
        projectsUsecase.isPermitted(projectId, owner);
    }

}
