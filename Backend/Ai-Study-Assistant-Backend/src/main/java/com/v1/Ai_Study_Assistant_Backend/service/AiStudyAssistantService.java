package com.v1.Ai_Study_Assistant_Backend.service;

import com.v1.Ai_Study_Assistant_Backend.dto.AskRequest;
import com.v1.Ai_Study_Assistant_Backend.dto.AskResponse;
import com.v1.Ai_Study_Assistant_Backend.dto.HistoryResponse;
import com.v1.Ai_Study_Assistant_Backend.entity.QueryHistory;
import com.v1.Ai_Study_Assistant_Backend.repository.QueryHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;


@Service
public class AiStudyAssistantService {

    private final RestClient restClient;
    private final QueryHistoryRepository queryHistoryRepository;

    public AiStudyAssistantService(@Value("${ai.microservice.base-url}") String fastApiUrl,
                                   QueryHistoryRepository queryHistoryRepository){
        this.restClient = RestClient.builder()
                .baseUrl(fastApiUrl)
                .build();
        this.queryHistoryRepository = queryHistoryRepository;
    }

    public AskResponse getAiAnswer(String userQuestion, String userEmail) {
        AskRequest request = new AskRequest(userQuestion, userEmail);


        // 1. Get the response from FastAPI
        AskResponse aiResponse = restClient.post()
                .uri("/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AskResponse.class);

        // 2. Map the data to our Entity
        QueryHistory history = new QueryHistory();
        history.setQuestion(userQuestion);
        history.setUserEmail(userEmail);

        if (aiResponse != null) {
            history.setAnswer(aiResponse.answer());
            history.setSources(aiResponse.sources());
        }

        // 3. Save to PostgreSQL
        queryHistoryRepository.save(history);

        // 4. Return the DTO back to the Controller (and React)
        return aiResponse;
    }

    public List<HistoryResponse> getChatHistory(String userEmail) {
        return queryHistoryRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(history -> new HistoryResponse(
                        history.getId(),
                        history.getQuestion(),
                        history.getAnswer(),
                        history.getSources(),
                        history.getCreatedAt()
                ))
                .toList(); // .toList() is available in Java 16+
    }
}
