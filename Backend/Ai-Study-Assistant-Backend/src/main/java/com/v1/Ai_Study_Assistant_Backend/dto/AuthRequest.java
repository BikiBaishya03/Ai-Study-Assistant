package com.v1.Ai_Study_Assistant_Backend.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
