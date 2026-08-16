package com.v1.Ai_Study_Assistant_Backend.dto;

import java.util.List;

public record AskResponse(
        String question,
        String answer,
        List<String> sources
) {}