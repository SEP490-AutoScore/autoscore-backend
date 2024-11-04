package com.CodeEvalCrew.AutoScore.services.gherkin_scenario_service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
                        question += "\n\n\n"
                                + "\n -Question Content:" + examQuestion.getQuestionContent()
                                + "\n -Role" + examQuestion.getRoleAllow()
                                + "\n" + examQuestion.getDescription();
                    }
    
                    String response = sendToAI(question, aiInfo.getAiApiKey());
                    responseBuilder.append(response).append("\n");
    
                    if (content.getOrderPriority() == 2) {
                        List<String> gherkinDataList = extractGherkinData(response);
                        saveGherkinData(gherkinDataList, examQuestion);
                    }
                });
            });
    
            // Thêm kết quả từ examQuestionId hiện tại vào tổng kết quả
            overallResponseBuilder.append("Exam Question ID: ").append(examQuestionId).append("\n")
                                  .append(responseBuilder.toString()).append("\n\n");
        }
    
        return overallResponseBuilder.toString();
    }
    
    // public String generateGherkinFormat(Long examQuestionId) {
    //     // Lấy AI_Info với purpose là "Generate GherkinFormat"
    //     List<AI_Info> aiInfos = aiInfoRepository.findByPurpose("Generate GherkinFormat");

    //     // Truy vấn Exam_Database dựa trên examQuestionId
    //     Exam_Database examDatabase = examDatabaseRepository.findByExamQuestionId(examQuestionId)
    //             .orElseThrow(() -> new RuntimeException("Exam Database không tồn tại"));
    //     String databaseScript = examDatabase.getDatabaseScript();

    //     // Truy vấn Exam_Question dựa trên examQuestionId
    //     Exam_Question examQuestion = examQuestionRepository.findById(examQuestionId)
    //             .orElseThrow(() -> new RuntimeException("Exam Question không tồn tại"));

    //     StringBuilder responseBuilder = new StringBuilder();
    //     aiInfos.forEach(aiInfo -> {
    //         List<Content> orderedContents = aiInfo.getContents()
    //                 .stream()
    //                 .sorted((c1, c2) -> Long.compare(c1.getOrderPriority(), c2.getOrderPriority()))
    //                 .collect(Collectors.toList());

    //         orderedContents.forEach(content -> {
    //             String question = content.getQuestionContent();
    //             if (content.getOrderPriority() == 1) {
    //                 question += "\n" + databaseScript;
    //             } else if (content.getOrderPriority() == 2) {
    //                 question += "\n\n\n"
    //                         + "\n -Question Content:" + examQuestion.getQuestionContent()
    //                         + "\n -Role" + examQuestion.getRoleAllow()
    //                         + "\n" + examQuestion.getDescription();
    //             }

    //             String response = sendToAI(question, aiInfo.getAiApiKey());
    //             responseBuilder.append(response).append("\n");

    //             if (content.getOrderPriority() == 2) {
    //                 List<String> gherkinDataList = extractGherkinData(response);
    //                 saveGherkinData(gherkinDataList, examQuestion);
    //             }
    //         });
    //     });

    //     return responseBuilder.toString();
    // }

    private List<String> extractGherkinData(String response) {
        List<String> gherkinDataList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);

        while (matcher.find()) {
            // Lấy dữ liệu giữa dấu {{ }}
            String gherkinData = matcher.group(1).trim();

            // Thay thế dấu ** và xuống dòng \n để chuẩn hóa cho MySQL
            gherkinData = gherkinData.replace("**", "  ") // Bỏ dấu ** để dễ đọc
                    .replace("\\n", "\n"); // Chuyển ký tự \\n thành dòng mới

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
