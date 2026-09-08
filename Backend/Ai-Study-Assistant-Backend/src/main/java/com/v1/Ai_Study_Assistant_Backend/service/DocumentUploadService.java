package com.v1.Ai_Study_Assistant_Backend.service;

import com.v1.Ai_Study_Assistant_Backend.dto.IngestionJob;
import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
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

    private JdbcTemplate jdbcTemplate;

    // The name of the queue Python will be listening to
    private static final String QUEUE_NAME = "pdf-ingestion-queue";
    public DocumentUploadService(MinioClient minioClient,
                                 RedisTemplate<String, Object> redisTemplate,
                                JdbcTemplate jdbcTemplate){
        this.minioClient = minioClient;
        this.redisTemplate = redisTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    public String uploadAndQueueProcessing(MultipartFile file, String userEmail) {
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
            IngestionJob job = new IngestionJob(bucketName, objectName, originalFilename, userEmail);

            // rightPush adds the job to the end of the list (FIFO queue)
            redisTemplate.opsForList().rightPush(QUEUE_NAME, job);

            // THE ONLY CHANGE: Return the objectName UUID instead of a sentence!
            return objectName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO or queue to Valkey", e);
        }
    }

    public void deleteDocumentResources(String objectName, String originalFilename, String userEmail) {
        try {
            // 1. Delete the physical file from MinIO
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            // 2. Delete the AI vectors from LangChain's pgvector table
            // LangChain stores our metadata in a JSONB column called 'cmetadata'
            String sql = "DELETE FROM langchain_pg_embedding " +
                    "WHERE cmetadata->>'source' = ? AND cmetadata->>'user_email' = ?";

            int deletedChunks = jdbcTemplate.update(sql, originalFilename, userEmail);
            System.out.println("Deleted " + deletedChunks + " vector chunks for " + originalFilename);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete document resources from MinIO or Vector DB", e);
        }
    }
}
