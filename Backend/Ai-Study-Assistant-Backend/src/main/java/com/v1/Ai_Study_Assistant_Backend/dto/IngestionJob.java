package com.v1.Ai_Study_Assistant_Backend.dto;

public record IngestionJob(
        String bucketName,
        String objectName,
        String originalFileName
) {}
