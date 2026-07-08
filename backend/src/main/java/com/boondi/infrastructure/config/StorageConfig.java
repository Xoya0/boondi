package com.boondi.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;

import java.net.URI;

@Slf4j
@Configuration
public class StorageConfig {

    @Value("${app.storage.endpoint}")
    private String endpoint;

    @Value("${app.storage.access-key}")
    private String accessKey;

    @Value("${app.storage.secret-key}")
    private String secretKey;

    @Value("${app.storage.region}")
    private String region;

    @Value("${app.storage.bucket}")
    private String bucket;

    @Bean
    public S3Client s3Client() {
        S3Client client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .forcePathStyle(true)   // required for MinIO
                .build();

        ensureBucketExists(client);
        ensurePublicReadPolicy(client);
        return client;
    }

    private void ensureBucketExists(S3Client client) {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("Storage bucket '{}' already exists", bucket);
        } catch (NoSuchBucketException e) {
            client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("Created storage bucket '{}'", bucket);
        } catch (Exception e) {
            log.warn("Could not verify/create storage bucket '{}': {}", bucket, e.getMessage());
        }
    }

    // MinIO buckets are private by default — without this, every avatar/banner/post-image
    // URL we hand back to clients (UploadResponse.url / app.storage.public-url) 403s for
    // every caller (web browser included, not just the Android emulator). Idempotent, so
    // it's safe/cheap to re-apply on every startup — also fixes buckets created before this
    // policy existed.
    private void ensurePublicReadPolicy(S3Client client) {
        try {
            String policy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Effect": "Allow",
                          "Principal": "*",
                          "Action": ["s3:GetObject"],
                          "Resource": ["arn:aws:s3:::%s/*"]
                        }
                      ]
                    }
                    """.formatted(bucket);
            client.putBucketPolicy(PutBucketPolicyRequest.builder()
                    .bucket(bucket)
                    .policy(policy)
                    .build());
            log.info("Public-read policy applied to storage bucket '{}'", bucket);
        } catch (Exception e) {
            log.warn("Could not apply public-read policy to storage bucket '{}': {}", bucket, e.getMessage());
        }
    }
}
