package com.v1.Ai_Study_Assistant_Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "query_history")
@Getter
@Setter
@NoArgsConstructor
public class QueryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;

    @ElementCollection
    @CollectionTable(name = "query_history_source", joinColumns = @JoinColumn(name = "query_history_id"))
    @Column(name = "source_file")
    private List<String> Sources;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
}
