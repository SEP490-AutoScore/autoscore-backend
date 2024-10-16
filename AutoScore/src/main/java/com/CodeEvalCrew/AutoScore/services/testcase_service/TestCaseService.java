package com.CodeEvalCrew.AutoScore.services.testcase_service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Barem;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamBaremRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class TestCaseService implements ITestCaseService {

    private final RestTemplate restTemplate;
    private final IExamDatabaseRepository examDatabaseRepository;
    private final IExamBaremRepository examBaremRepository;

    public TestCaseService(RestTemplate restTemplate, IExamDatabaseRepository examDatabaseRepository, IExamBaremRepository examBaremRepository) {
        this.restTemplate = restTemplate;
        this.examDatabaseRepository = examDatabaseRepository;
        this.examBaremRepository = examBaremRepository;
    }

    @Override
    public String getAIResponse(Long examDatabaseId, Long examBaremId, int minimumNumberOfTestcases) {
        // Fetch databaseScript from Exam_Database using examDatabaseId
        Exam_Database examDatabase = examDatabaseRepository.findById(examDatabaseId)
            .orElseThrow(() -> new RuntimeException("Exam Database not found"));
        String databaseScript = examDatabase.getDatabaseScript();

        // Fetch baremContent and baremURL from Exam_Barem using examBaremId
        Exam_Barem examBarem = examBaremRepository.findById(examBaremId)
            .orElseThrow(() -> new RuntimeException("Exam Barem not found"));
        String baremContent = examBarem.getBaremContent();
        String baremURL = examBarem.getBaremURL();

        // Construct AI prompt
        String prompt = String.format(
            "- viết ít nhất %d testcase cho hàm %s, %s\n- xuất file collection postman cho tôi\n- dữ liệu file collection postman cần có \"info\": \"_postman_id\": \"name\": \"schema\": \"_postman_workspace_id\": \"description\": \"_postman_exported_at\": \"_postman_export_type\": \"item\": \n- \"item\" cần có \"event\", \"listen\": \"test\", các \"script\": \"exec\": \"type\": \"text/javascript\"\nDưới đây là file .sql:\n%s",
            minimumNumberOfTestcases, baremContent, baremURL, databaseScript
        );

        // Set up the request to the AI service
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=AIzaSyDxNBkQgMw5bxnB47_NLI5dnmiwKoRPqJc";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode contentNode = objectMapper.createObjectNode();
        ObjectNode partsNode = objectMapper.createObjectNode();
        partsNode.put("text", prompt);
        contentNode.set("parts", objectMapper.createArrayNode().add(partsNode));
        ObjectNode requestBodyNode = objectMapper.createObjectNode();
        requestBodyNode.set("contents", objectMapper.createArrayNode().add(contentNode));

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(requestBodyNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct JSON request body", e);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

        // Log the raw response body for debugging
        String responseBody = response.getBody();
        System.out.println("Response Body: " + responseBody);

        // Remove the backticks and "```json" from the response body
        String cleanedResponseBody = responseBody.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            // Parse the cleaned response
            JsonNode rootNode = objectMapper.readTree(cleanedResponseBody);
            JsonNode contentNodeResponse = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");

            // Check if content.parts.text exists and is valid
            if (contentNodeResponse.isMissingNode() || contentNodeResponse.asText().isEmpty()) {
                throw new RuntimeException("Expected field 'content.parts.text' is missing or empty");
            }

            // Convert the extracted text (which is a JSON string) into a JSON object
            JsonNode jsonNodeFromText = objectMapper.readTree(contentNodeResponse.asText());

            // Write the JSON object into a file (e.g., D:\Desktop\collection_postman.json)
            String outputFilePath = "D:\\Desktop\\collection_postman.json";
            Files.write(Paths.get(outputFilePath), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(jsonNodeFromText));

            return "JSON file created successfully at: " + outputFilePath;

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse or save JSON content", e);
        }
    }
}
