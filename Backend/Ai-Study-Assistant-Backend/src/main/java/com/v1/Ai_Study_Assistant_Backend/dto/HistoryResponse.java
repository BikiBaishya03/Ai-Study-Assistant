package com.v1.Ai_Study_Assistant_Backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record HistoryResponse(
        Long id,
        String question,
        String answer,
        List<String> sources,
        LocalDateTime createdAt
) {}
