package com.v1.Ai_Study_Assistant_Backend.dto;

public record IngestionJob(
        Long docId,
        String bucketName,
        String objectName,
        String originalFileName,
        String userEmail
) {}
