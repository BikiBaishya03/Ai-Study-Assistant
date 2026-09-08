package com.v1.Ai_Study_Assistant_Backend.repository;

import com.v1.Ai_Study_Assistant_Backend.entity.QueryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryHistoryRepository extends JpaRepository<QueryHistory, Long> {
    List<QueryHistory> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
