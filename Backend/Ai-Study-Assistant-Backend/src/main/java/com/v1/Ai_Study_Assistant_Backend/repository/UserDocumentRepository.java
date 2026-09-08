package com.v1.Ai_Study_Assistant_Backend.repository;

import com.v1.Ai_Study_Assistant_Backend.entity.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {
    List<UserDocument> findByUserEmail(String userEmail);
}
