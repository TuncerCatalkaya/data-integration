package org.dataintegration.usecase;

import lombok.RequiredArgsConstructor;
import org.dataintegration.exception.runtime.ScopeNotFoundException;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.model.CompletedPartModel;
import org.dataintegration.service.ProjectsService;
import org.dataintegration.service.S3Service;
import org.dataintegration.service.ScopesService;
import org.dataintegration.usecase.model.GeneratePresignedUrlResponseModel;
import org.dataintegration.usecase.model.InitiateMultipartUploadRequestModel;
import org.dataintegration.usecase.model.S3ListResponseModel;
import org.dataintegration.utils.DataIntegrationUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Usecase {

    private final ProjectsService projectsService;
    private final ScopesService scopesService;
    private final S3Service s3Service;

    public InitiateMultipartUploadRequestModel initiateMultipartUpload(String bucket, String key, String createdBy) {
        isPermitted(key, createdBy);
        return InitiateMultipartUploadRequestModel.builder()
                .uploadId(s3Service.initiateMultipartUpload(bucket, key))
                .build();
    }

    public GeneratePresignedUrlResponseModel generatePresignedUrlMultiPartUpload(String bucket, String key, String uploadId,
                                                                                 int partNumber, String createdBy) {
        isPermitted(key, createdBy);
        return GeneratePresignedUrlResponseModel.builder()
                .presignedUrl(s3Service.generatePresignedUrlMultiPartUpload(bucket, key, uploadId, partNumber))
                .build();
    }

    public void completeMultipartUpload(String bucket, String key, String uploadId, long lineCount, char delimiter,
                                        List<CompletedPartModel> completedParts, String createdBy) {
        isPermitted(key, createdBy);
        s3Service.completeMultipartUpload(bucket, key, uploadId, lineCount, delimiter, completedParts);
    }

    public void abortMultipartUpload(String bucket, String key, String uploadId, String createdBy) {
        isPermitted(key, createdBy);
        s3Service.abortMultipartUpload(bucket, key, uploadId);
    }

    public List<S3ListResponseModel> listObjectsV2(String bucket, String projectId, String createdBy) {
        isPermitted(projectId, createdBy);
        final ListObjectsV2Response listObjectsV2Response = s3Service.listObjectsV2(bucket, projectId);
        return listObjectsV2Response.contents().stream()
                .map(s3Object -> {
                    try {
                        final ScopeEntity scopeEntity = scopesService.get(DataIntegrationUtils.getProjectIdFromS3Key(s3Object.key()),
                                        DataIntegrationUtils.getScopeKeyFromS3Key(s3Object.key()))
                                .orElse(null);
                        return S3ListResponseModel.builder()
                                .key(s3Object.key())
                                .lastModified(Date.from(s3Object.lastModified()))
                                .size(s3Object.size())
                                .checkpoint(scopeEntity != null && scopeEntity.getCheckpoint() != null)
                                .build();
                    } catch (ScopeNotFoundException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public void deleteObject(String bucket, String key, String createdBy) {
        isPermitted(key, createdBy);
        s3Service.deleteObject(bucket, key);
    }

    private void isPermitted(String key, String createdBy) {
        final UUID projectId = DataIntegrationUtils.getProjectIdFromS3Key(key);
        projectsService.isPermitted(projectId, createdBy);
    }

}
