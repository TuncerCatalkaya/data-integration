package org.dataintegration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dataintegration.model.CompletedPartModel;
import org.dataintegration.usecase.S3Usecase;
import org.dataintegration.usecase.model.GeneratePresignedUrlResponseModel;
import org.dataintegration.usecase.model.InitiateMultipartUploadRequestModel;
import org.dataintegration.usecase.model.S3ListResponseModel;
import org.dataintegration.utils.DataIntegrationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "/s3")
@RestController
@RequestMapping("${server.root.path}/s3")
@RequiredArgsConstructor
public class S3RestController {

    private final S3Usecase s3Usecase;

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PostMapping("/multipart-upload/initiate")
    public InitiateMultipartUploadRequestModel initiateMultipartUpload(@AuthenticationPrincipal Jwt jwt,
                                                                       @RequestParam String bucket, @RequestParam String key) {
        return s3Usecase.initiateMultipartUpload(bucket, key, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PostMapping("/multipart-upload/complete")
    public void completeMultipartUpload(@AuthenticationPrincipal Jwt jwt, @RequestParam String bucket, @RequestParam String key,
                                        @RequestParam String uploadId, @RequestParam long lineCount,
                                        @RequestParam String delimiter, @RequestBody List<CompletedPartModel> completedParts) {
        s3Usecase.completeMultipartUpload(bucket, key, uploadId, lineCount,
                DataIntegrationUtils.delimiterStringToCharMapper(delimiter), completedParts, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @PostMapping("/multipart-upload/abort")
    public void abortMultipartUpload(@AuthenticationPrincipal Jwt jwt, @RequestParam String bucket, @RequestParam String key,
                                     @RequestParam String uploadId) {
        s3Usecase.abortMultipartUpload(bucket, key, uploadId, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @GetMapping("/multipart-upload/presigned-url")
    public GeneratePresignedUrlResponseModel generatePresignedUrlMultiPartUpload(@AuthenticationPrincipal Jwt jwt,
                                                                                 @RequestParam String bucket,
                                                                                 @RequestParam String key,
                                                                                 @RequestParam String uploadId,
                                                                                 @RequestParam int partNumber) {
        return s3Usecase.generatePresignedUrlMultiPartUpload(bucket, key, uploadId, partNumber,
                DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @GetMapping("/objects")
    public List<S3ListResponseModel> listObjectsV2(@AuthenticationPrincipal Jwt jwt, @RequestParam String bucket,
                                                   @RequestParam String projectId) {
        return s3Usecase.listObjectsV2(bucket, projectId, DataIntegrationUtils.getJwtUserId(jwt));
    }

    @PreAuthorize("hasRegexAuthority(@authorityConfig.authorityRegexes)")
    @DeleteMapping("/objects")
    public void deleteObject(@AuthenticationPrincipal Jwt jwt, @RequestParam String bucket, @RequestParam String key) {
        s3Usecase.deleteObject(bucket, key, DataIntegrationUtils.getJwtUserId(jwt));
    }

}
