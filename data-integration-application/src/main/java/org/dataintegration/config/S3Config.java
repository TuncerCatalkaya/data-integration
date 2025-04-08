package org.dataintegration.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@DependsOn("frontendDotEnvModel")
@Slf4j
public class S3Config {

    @Value("${s3.region}")
    private String s3Region;

    @Value("${s3.endpoint.client}")
    private String s3EndpointClient;

    @Value("${s3.endpoint.presigner}")
    private String s3EndpointPresigner;

    @Value("${s3.access-key}")
    private String s3AccessKey;

    @Value("${s3.secret-key}")
    private String s3SecretKey;

    @Value("${s3.path-style-access-enabled}")
    private boolean pathStyleAccessEnabled;

    @Value("${s3.bucket}")
    private String bucket;

    @Value("${s3.enabled}")
    private boolean enabled;

    @Bean
    S3Client s3Client() {
        final S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyleAccessEnabled)
                .build();
        return S3Client.builder()
                .region(Region.of(s3Region))
                .endpointOverride(URI.create(s3EndpointClient))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey)))
                .serviceConfiguration(s3Configuration)
                .build();
    }

    @Bean
    S3Presigner s3Presigner() {
        final S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyleAccessEnabled)
                .build();
        return S3Presigner.builder()
                .region(Region.of(s3Region))
                .endpointOverride(URI.create(s3EndpointPresigner))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey)))
                .serviceConfiguration(s3Configuration)
                .build();
    }

    /**
     * On spring boot start-up.
     * Check if "data-integration" bucket exists. If not then an exception will be thrown and the bucket will be created.
     *
     * @param s3Client s3 client
     * @return {@link ApplicationRunner}
     */
    @Bean
    ApplicationRunner initializeS3(S3Client s3Client) {
        return args -> {
            if (enabled) {
                try {
                    final HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                            .bucket(bucket)
                            .build();
                    s3Client.headBucket(headBucketRequest);
                } catch (S3Exception ex) {
                    final CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                            .bucket(bucket)
                            .build();
                    s3Client.createBucket(createBucketRequest);
                } catch (SdkClientException ex) {
                    log.warn(
                            "S3 connection could not be established. "
                            + "If you don't want to use S3, then disable it by setting the environment variable: "
                            + "s3.enabled=false (S3_ENABLED=false)"
                    );
                    throw ex;
                }
            }
        };
    }

}
