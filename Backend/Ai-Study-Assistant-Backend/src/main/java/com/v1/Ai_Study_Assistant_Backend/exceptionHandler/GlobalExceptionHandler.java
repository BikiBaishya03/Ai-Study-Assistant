package com.v1.Ai_Study_Assistant_Backend.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<String> handleFastApiErrors(RestClientException ex) {
        // Logs the error on your Spring Boot console
        System.err.println("FastAPI Microservice Error: " + ex.getMessage());

        // Sends a clean error message back to React
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("The AI Study Assistant is currently unreachable. Please try again later.");
    }
}
