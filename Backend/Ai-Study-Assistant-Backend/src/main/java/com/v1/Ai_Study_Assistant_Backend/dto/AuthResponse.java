package com.v1.Ai_Study_Assistant_Backend.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    public AuthResponse(String token) {
        this.token = token;
    }
}
