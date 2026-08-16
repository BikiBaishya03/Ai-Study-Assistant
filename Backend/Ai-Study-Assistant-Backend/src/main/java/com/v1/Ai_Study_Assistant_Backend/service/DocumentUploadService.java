package com.v1.Ai_Study_Assistant_Backend.service;

import com.v1.Ai_Study_Assistant_Backend.dto.IngestionJob;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class DocumentUploadService {
    private final MinioClient minioClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${minio.bucket.name}")
    private String bucketName;

    // The name of the queue Python will be listening to
    private static final String QUEUE_NAME = "pdf-ingestion-queue";
    public DocumentUploadService(MinioClient minioClient, RedisTemplate<String, Object> redisTemplate){
        this.minioClient = minioClient;
        this.redisTemplate = redisTemplate;
    }

    public String uploadAndQueueProcessing(MultipartFile file) {
        try {
            // 1. Generate a unique object name (e.g., "550e8400-e29b-41d4-a716-446655440000_notes.pdf")
            String originalFilename = file.getOriginalFilename();
            String objectName = UUID.randomUUID() + "_" + originalFilename;

            // 2. Automate Bucket Creation: Check if bucket exists, create if not
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Created new MinIO bucket: " + bucketName);
            }

            // 3. Upload the file to MinIO
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            // 4. Create the job payload and push to Valkey (Redis) queue
            IngestionJob job = new IngestionJob(bucketName, objectName, originalFilename);

            // rightPush adds the job to the end of the list (FIFO queue)
            redisTemplate.opsForList().rightPush(QUEUE_NAME, job);

            return "File uploaded successfully and queued for processing: " + originalFilename;

        } catch (Exception e) {
            // In a production app, you might want to throw a custom exception here
            throw new RuntimeException("Failed to upload file to MinIO or queue to Valkey", e);
        }
    }
}
