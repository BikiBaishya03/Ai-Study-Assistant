package com.v1.Ai_Study_Assistant_Backend.controller;

import com.v1.Ai_Study_Assistant_Backend.dto.AskResponse;
import com.v1.Ai_Study_Assistant_Backend.dto.HistoryResponse;
import com.v1.Ai_Study_Assistant_Backend.service.DocumentUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.v1.Ai_Study_Assistant_Backend.service.AiStudyAssistantService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/study")
@CrossOrigin(origins = "http://localhost:3000")
public class AiStudyAssistantController {
    private final AiStudyAssistantService aiService;
    private final DocumentUploadService documentUploadService;
    public AiStudyAssistantController(AiStudyAssistantService aiService,
                                      DocumentUploadService documentUploadService){
        this.aiService = aiService;
        this.documentUploadService = documentUploadService;
    }

    @PostMapping("/ask")
    public AskResponse askAi(@RequestBody String question){
        return aiService.getAiAnswer(question);
    }

    @GetMapping("/history")
    public List<HistoryResponse> getHistory() {
        return aiService.getChatHistory();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        // Basic validation
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().body("Only PDF files are allowed.");
        }

        String result = documentUploadService.uploadAndQueueProcessing(file);
        return ResponseEntity.ok(result);
    }
}
