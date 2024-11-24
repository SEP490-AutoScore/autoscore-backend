package com.CodeEvalCrew.AutoScore.services.grading_service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Grading.GradingRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GradingService implements IGradingService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void startingGradingProcess(GradingRequest request) throws Exception {
        try {
            // Tạo HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Chuyển đối tượng request thành JSON
            String requestBody = objectMapper.writeValueAsString(request);

            // Tạo HttpRequest
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/grading")) // Đổi URL theo đúng endpoint của bạn
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Gửi request và chờ phản hồi
            client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());

        } catch (JsonProcessingException e) {
            throw e;
        }
    }

}
