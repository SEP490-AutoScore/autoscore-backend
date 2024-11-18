package com.CodeEvalCrew.AutoScore.services.gherkin_scenario_service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.CodeEvalCrew.AutoScore.models.Entity.AI_Info;
import com.CodeEvalCrew.AutoScore.models.Entity.Content;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;
import com.CodeEvalCrew.AutoScore.repositories.ai_info_repository.AIInfoRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.gherkin_scenario_repository.GherkinScenarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.transaction.Transactional;

@Service
public class GherkinScenarioService implements IGherkinScenarioService {

    @Autowired
    private AIInfoRepository aiInfoRepository;
    @Autowired
    private IExamDatabaseRepository examDatabaseRepository;
    @Autowired
    private IExamQuestionRepository examQuestionRepository;
    @Autowired
    private GherkinScenarioRepository gherkinScenarioRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String getAllGherkinScenariosByExamQuestionId(Long examQuestionId) {
        if (examQuestionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam Question ID is required.");
        }
    
        // Lấy danh sách Gherkin_Scenario theo examQuestionId
        List<Gherkin_Scenario> scenarios = gherkinScenarioRepository.findByExamQuestion_ExamQuestionIdOrderByOrderPriorityAsc(examQuestionId);
    
        // Kiểm tra nếu không tìm thấy kết quả
        if (scenarios.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Gherkin Scenarios found for the provided Exam Question ID.");
        }
    
        // Gộp các GherkinData thành chuỗi
        return scenarios.stream()
                .map(Gherkin_Scenario::getGherkinData)
                .map(this::trimEdges)
                .collect(Collectors.joining("\n\n")); // Cách nhau bởi 1 dòng trống
    }
    
    //Hàm xóa ký tự xuống dòng đầu và cuối của một chuỗi nếu tồn tại.
     
    private String trimEdges(String gherkinData) {
        if (gherkinData == null) {
            return "";
        }
        if (gherkinData.startsWith("\n")) {
            gherkinData = gherkinData.substring(1);
        }
        if (gherkinData.endsWith("\n")) {
            gherkinData = gherkinData.substring(0, gherkinData.length() - 1);
        }
        return gherkinData;
    }

    
    // @Override
    // public List<GherkinFormatDTO> getAllGherkinScenariosByExamQuestionId(Long examQuestionId) {
    //     if (examQuestionId == null) {
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam Question ID is required.");
    //     }

    //     return gherkinScenarioRepository.findByExamQuestion_ExamQuestionIdOrderByOrderPriorityAsc(examQuestionId)
    //             .stream()
    //             .map(this::convertToDTO)
    //             .collect(Collectors.toList());
    // }

    // private GherkinFormatDTO convertToDTO(Gherkin_Scenario scenario) {
    //     return new GherkinFormatDTO(
    //             scenario.getGherkinScenarioId(),
    //             scenario.getGherkinData(),
    //             scenario.getOrderPriority(),
    //             scenario.getIsUpdateCreate(),
    //             scenario.getExamQuestion().getExamQuestionId());
    // }

    @Override
    @Transactional
    public String generateGherkinFormat(List<Long> examQuestionIds) {
        StringBuilder overallResponseBuilder = new StringBuilder();

        for (Long examQuestionId : examQuestionIds) {
            // Lấy AI_Info với purpose là "Generate GherkinFormat"
            List<AI_Info> aiInfos = aiInfoRepository.findByPurpose("Generate GherkinFormat");

            // Truy vấn Exam_Database dựa trên examQuestionId
            Exam_Database examDatabase = examDatabaseRepository.findByExamQuestionId(examQuestionId)
                    .orElseThrow(() -> new RuntimeException("Exam Database không tồn tại"));

            String databaseScript = examDatabase.getDatabaseScript();
            System.out.println("Database Script: " + databaseScript);

            // Truy vấn Exam_Question dựa trên examQuestionId
            Exam_Question examQuestion = examQuestionRepository.findById(examQuestionId)
                    .orElseThrow(() -> new RuntimeException("Exam Question không tồn tại"));

            StringBuilder responseBuilder = new StringBuilder();

            // Tạo đoạn chat mới cho mỗi examQuestionId
            aiInfos.forEach(aiInfo -> {
                List<Content> orderedContents = aiInfo.getContents()
                        .stream()
                        .sorted((c1, c2) -> Long.compare(c1.getOrderPriority(), c2.getOrderPriority()))
                        .collect(Collectors.toList());

                orderedContents.forEach(content -> {
                    String question = content.getQuestionContent();
                    if (content.getOrderPriority() == 1) {
                        question += "\n" + databaseScript;
                    } else if (content.getOrderPriority() == 2) {
                        // question += "\n\n\n"
                        question += ""
                                + "\n - Question Content: " + examQuestion.getQuestionContent()
                                + "\n - Role: " + examQuestion.getRoleAllow()
                                + "\n - Description: " + examQuestion.getDescription()
                                + "\n - End point: " + examQuestion.getEndPoint()
                                + "\n - Http method: " + examQuestion.getHttpMethod()
                                + "\n - Payload type: " + examQuestion.getPayloadType()
                                + "\n - Validation: " + examQuestion.getValidation()
                                + "\n - Success response: " + examQuestion.getSucessResponse()
                                + "\n - Error response: " + examQuestion.getErrorResponse()
                                + "\n - Payload: " + examQuestion.getPayload();
                    }

                    String promptInUTF8 = new String(question.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    String response = sendToAI(promptInUTF8, aiInfo.getAiApiKey());

                    // String response = sendToAI(question, aiInfo.getAiApiKey());
                    responseBuilder.append(response).append("\n");

                    if (content.getOrderPriority() == 2) {
                        List<String> gherkinDataList = extractGherkinData(response);
                        saveGherkinData(gherkinDataList, examQuestion);
                        // extractGherkinDataAndSave(response, examQuestion);
                    }
                });
            });

            // Thêm kết quả từ examQuestionId hiện tại vào tổng kết quả
            overallResponseBuilder.append("Exam Question ID: ").append(examQuestionId).append("\n")
                    .append(responseBuilder.toString()).append("\n\n");
        }

        return overallResponseBuilder.toString();
    }

    private List<String> extractGherkinData(String response) {
        List<String> gherkinDataList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);

        while (matcher.find()) {
            // Lấy dữ liệu giữa dấu {{ }}
            String gherkinData = matcher.group(1).trim();

            // Thay thế dấu ** và xuống dòng \n để chuẩn hóa cho MySQL
            gherkinData = gherkinData.replace("**", "  ") // Bỏ dấu ** để dễ đọc
                    .replace("\\n", "\n") // Chuyển ký tự \\n thành dòng mới
                    .replace("\"", "")
                    .replace("\\", "");

            // Loại bỏ \n đầu và cuối chuỗi nếu có
            gherkinData = gherkinData.replaceAll("^\\n+|\\n+$", "").trim();

            gherkinDataList.add(gherkinData);
        }
        return gherkinDataList;
    }

    private void saveGherkinData(List<String> gherkinDataList, Exam_Question examQuestion) {
        long priority = 1;
        for (String data : gherkinDataList) {
            Gherkin_Scenario scenario = new Gherkin_Scenario();
            scenario.setGherkinData(data);
            scenario.setOrderPriority(priority++);
            scenario.setExamQuestion(examQuestion);
            scenario.setIsUpdateCreate(true);

            gherkinScenarioRepository.save(scenario);
        }
    }

    private String sendToAI(String prompt, String aiApiKey) {
        // Set up the request to the AI service
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key="
                + aiApiKey;

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
            throw new RuntimeException("Failed to construct AI request", e);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

        // Log the raw response body for debugging
        String responseBody = response.getBody();
        System.out.println("Response Body: " + responseBody);

        return responseBody;
    }
}
