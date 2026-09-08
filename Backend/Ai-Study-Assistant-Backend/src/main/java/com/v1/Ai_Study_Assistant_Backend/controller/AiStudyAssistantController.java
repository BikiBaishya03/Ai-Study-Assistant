package com.v1.Ai_Study_Assistant_Backend.controller;

import com.v1.Ai_Study_Assistant_Backend.dto.AskResponse;
import com.v1.Ai_Study_Assistant_Backend.dto.HistoryResponse;
import com.v1.Ai_Study_Assistant_Backend.entity.User;
import com.v1.Ai_Study_Assistant_Backend.entity.UserDocument;
import com.v1.Ai_Study_Assistant_Backend.repository.UserDocumentRepository;
import com.v1.Ai_Study_Assistant_Backend.service.DocumentUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.v1.Ai_Study_Assistant_Backend.service.AiStudyAssistantService;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/study")
@CrossOrigin(origins = "http://localhost:3000")
public class AiStudyAssistantController {
    private final AiStudyAssistantService aiService;
    private final DocumentUploadService documentUploadService;
    private final UserDocumentRepository documentRepository;
    public AiStudyAssistantController(AiStudyAssistantService aiService,
                                      DocumentUploadService documentUploadService,
                                      UserDocumentRepository documentRepository){
        this.aiService = aiService;
        this.documentUploadService = documentUploadService;
        this.documentRepository = documentRepository;
    }

    @PostMapping("/ask")
    public AskResponse askAi(@RequestBody String question, Principal principal){
        String userEmail = principal.getName();
        return aiService.getAiAnswer(question, userEmail);
    }

    @GetMapping("/history")
    public List<HistoryResponse> getHistory(Principal principal) {
        String userEmail = principal.getName();
        return aiService.getChatHistory(userEmail);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file, Principal principal) {

        // 1. Validation: Check if the file is empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please select a file to upload."));
        }

        // 2. Validation: Ensure it is a PDF
        if (!"application/pdf".equals(file.getContentType())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only PDF files are allowed."));
        }

        try {
            String userEmail = principal.getName();

            // 3. Upload to MinIO and push to Valkey (Returns the UUID)
            String objectName = documentUploadService.uploadAndQueueProcessing(file, userEmail);

            // 4. Save the record to the PostgreSQL database
            UserDocument doc = new UserDocument();
            doc.setFileName(file.getOriginalFilename());
            doc.setObjectName(objectName);
            doc.setUserEmail(userEmail);

            UserDocument savedDoc = documentRepository.save(doc);

            // 5. Return the saved document object back to React
            return ResponseEntity.ok(savedDoc);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "An error occurred during upload: " + e.getMessage()));
        }
    }

    @GetMapping("/documents")
    public ResponseEntity<List<UserDocument>> getUserDocuments(Principal principal) {
        String userEmail = principal.getName();
        List<UserDocument> docs = documentRepository.findByUserEmail(userEmail);
        return ResponseEntity.ok(docs);
    }

    @DeleteMapping("/document/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id, Principal principal) {
        try {
            String userEmail = principal.getName();

            // 1. Find the document in the database
            Optional<UserDocument> optionalDoc = documentRepository.findById(id);
            if (optionalDoc.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            UserDocument doc = optionalDoc.get();

            // 2. SECURITY LOCK: Ensure the user actually owns this file!
            if (!doc.getUserEmail().equals(userEmail)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You do not have permission to delete this document."));
            }

            // 3. Delete from MinIO and Vector Database
            documentUploadService.deleteDocumentResources(
                    doc.getObjectName(),
                    doc.getFileName(),
                    userEmail
            );

            // 4. Delete the record from PostgreSQL (user_documents table)
            documentRepository.delete(doc);

            return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "An error occurred during deletion: " + e.getMessage()));
        }
    }
}
