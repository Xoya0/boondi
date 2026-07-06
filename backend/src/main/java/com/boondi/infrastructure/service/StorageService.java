package com.boondi.infrastructure.service;

import com.boondi.infrastructure.exception.BoondiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;

    @Value("${app.storage.bucket}")
    private String bucket;

    @Value("${app.storage.public-url}")
    private String publicUrl;

    /**
     * Uploads a file to object storage and returns its public URL.
     */
    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));

            String fileUrl = publicUrl + "/" + bucket + "/" + key;
            log.info("Uploaded file to storage: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("Failed to upload file with key '{}': {}", key, e.getMessage());
            throw BoondiException.fileUploadFailed(e.getMessage());
        }
    }

    /**
     * Deletes a file from object storage by its key.
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.info("Deleted file from storage: {}", key);
        } catch (Exception e) {
            log.warn("Failed to delete file with key '{}': {}", key, e.getMessage());
        }
    }
}
