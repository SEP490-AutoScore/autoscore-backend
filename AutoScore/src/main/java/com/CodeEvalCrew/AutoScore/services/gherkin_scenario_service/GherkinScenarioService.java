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

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinPostmanPairDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinScenarioDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Info;
import com.CodeEvalCrew.AutoScore.models.Entity.Content;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;
import com.CodeEvalCrew.AutoScore.repositories.ai_info_repository.AIInfoRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.gherkin_scenario_repository.GherkinScenarioRepository;
import com.CodeEvalCrew.AutoScore.repositories.postman_for_grading.PostmanForGradingRepository;
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
    @Autowired
    private PostmanForGradingRepository postmanForGradingRepository;

    @Override
    public List<GherkinPostmanPairDTO> getAllGherkinAndPostmanPairs(Long examPaperId) {
        // Lấy tất cả Exam_Question theo examPaperId
        List<Exam_Question> examQuestions = examQuestionRepository.findByExamPaper_ExamPaperId(examPaperId);

        // Lấy tất cả Postman_For_Grading theo examPaperId và status = true
        List<Postman_For_Grading> postmanForGradings = postmanForGradingRepository
                .findByExamPaper_ExamPaperIdAndStatusTrue(examPaperId);

        List<GherkinPostmanPairDTO> pairs = new ArrayList<>();

        for (Exam_Question examQuestion : examQuestions) {
            // Lấy tất cả Gherkin_Scenario liên quan và có status = true
            List<Gherkin_Scenario> gherkinScenarios = gherkinScenarioRepository
                    .findByExamQuestionAndStatusTrue(examQuestion);

            // Tạo cặp Gherkin và Postman nếu có liên kết
            for (Gherkin_Scenario gherkin : gherkinScenarios) {
                Postman_For_Grading matchedPostman = postmanForGradings.stream()
                        .filter(postman -> postman.getGherkinScenario() != null
                                && postman.getGherkinScenario().getGherkinScenarioId()
                                        .equals(gherkin.getGherkinScenarioId()))
                        .findFirst()
                        .orElse(null);

                GherkinDTO gherkinDTO = new GherkinDTO(
                        gherkin.getGherkinScenarioId(),
                        gherkin.getGherkinData(),
                        gherkin.getOrderPriority(),
                        gherkin.getIsUpdateCreate(),
                        gherkin.getStatus(),
                        examQuestion.getExamQuestionId(),
                        matchedPostman != null ? matchedPostman.getPostmanForGradingId() : null);

                PostmanDTO postmanDTO = matchedPostman != null
                        ? new PostmanDTO(
                                matchedPostman.getPostmanForGradingId(),
                                matchedPostman.getPostmanFunctionName(),
                                matchedPostman.getScoreOfFunction(),
                                matchedPostman.getTotalPmTest(),
                                matchedPostman.getStatus(),
                                matchedPostman.getOrderBy(),
                                matchedPostman.getPostmanForGradingParentId(),
                                matchedPostman.getFileCollectionPostman(),
                                matchedPostman.getExamQuestion() != null
                                        ? matchedPostman.getExamQuestion().getExamQuestionId()
                                        : null,
                                matchedPostman.getGherkinScenario() != null
                                        ? matchedPostman.getGherkinScenario().getGherkinScenarioId()
                                        : null,
                                matchedPostman.getExamPaper() != null ? matchedPostman.getExamPaper().getExamPaperId()
                                        : null)
                        : null;

                pairs.add(new GherkinPostmanPairDTO(gherkinDTO, postmanDTO));
            }
        }

        // Xử lý trường hợp chỉ có Postman mà không có Gherkin
        for (Postman_For_Grading postman : postmanForGradings) {
            if (postman.getGherkinScenario() == null) {
                PostmanDTO postmanDTO = new PostmanDTO(
                        postman.getPostmanForGradingId(),
                        postman.getPostmanFunctionName(),
                        postman.getScoreOfFunction(),
                        postman.getTotalPmTest(),
                        postman.getStatus(),
                        postman.getOrderBy(),
                        postman.getPostmanForGradingParentId(),
                        postman.getFileCollectionPostman(),
                        postman.getExamQuestion() != null ? postman.getExamQuestion().getExamQuestionId() : null,
                        null, // Không có Gherkin Scenario
                        postman.getExamPaper() != null ? postman.getExamPaper().getExamPaperId() : null);

                pairs.add(new GherkinPostmanPairDTO(null, postmanDTO));
            }
        }

        return pairs;
    }

    @Override
    public List<GherkinScenarioDTO> getAllGherkinScenariosByExamPaperId(Long examPaperId) {
        // Lấy danh sách các Gherkin_Scenario từ repository
        List<Gherkin_Scenario> scenarios = gherkinScenarioRepository
                .findByExamQuestion_ExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriority(examPaperId);

        // Chuyển đổi từ Entity sang DTO
        return scenarios.stream().map(scenario -> new GherkinScenarioDTO(
                scenario.getGherkinScenarioId(),
                scenario.getGherkinData(),
                scenario.getOrderPriority(),
                scenario.getIsUpdateCreate(),
                scenario.getStatus(),
                scenario.getExamQuestion().getExamQuestionId(),
                scenario.getPostmanForGrading() != null ? scenario.getPostmanForGrading().getPostmanForGradingId()
                        : null))
                .collect(Collectors.toList());
    }

    @Override
    public void updateGherkinScenarios(Long examQuestionId, String gherkinDataBody) {
        if (examQuestionId == null || gherkinDataBody == null || gherkinDataBody.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input data.");
        }

        // Tách các Gherkin Data bằng dấu [<br>]
        String[] gherkinDataArray = gherkinDataBody.split("\\[<br>]");

        // Lấy examQuestion từ ID
        Exam_Question examQuestion = examQuestionRepository.findById(examQuestionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam Question not found"));

        // Đặt status của tất cả Gherkin_Scenario của examQuestion này thành false
        List<Gherkin_Scenario> existingScenarios = gherkinScenarioRepository
                .findByExamQuestion_ExamQuestionId(examQuestionId);
        for (Gherkin_Scenario scenario : existingScenarios) {
            scenario.setStatus(false);
        }
        gherkinScenarioRepository.saveAll(existingScenarios);

        // Tạo mới danh sách Gherkin_Scenario từ các Gherkin Data đã tách
        List<Gherkin_Scenario> newScenarios = new ArrayList<>();
        long orderPriority = 1; // Thiết lập orderPriority bắt đầu từ 1

        for (String gherkinData : gherkinDataArray) {
            // Loại bỏ các ký tự thừa
            String trimmedData = gherkinData.trim();

            // In ra gherkinData để kiểm tra
            System.out.println("Gherkin Data: " + trimmedData);

            // Bỏ qua nếu chuỗi trống
            if (trimmedData.isEmpty()) {
                continue;
            }

            // Tạo mới bản ghi Gherkin_Scenario
            Gherkin_Scenario scenario = new Gherkin_Scenario();
            scenario.setGherkinData(trimmedData);
            scenario.setOrderPriority(orderPriority++);
            scenario.setStatus(true); // Bản ghi mới có trạng thái true
            scenario.setExamQuestion(examQuestion);

            newScenarios.add(scenario);
        }

        // Lưu tất cả Gherkin_Scenario mới vào cơ sở dữ liệu
        gherkinScenarioRepository.saveAll(newScenarios);
    }

    @Override
    public String generateGherkinFormat(List<Long> examQuestionIds) {
        
      // Kiểm tra nếu đã có Gherkin cho bất kỳ examQuestionId nào
      for (Long examQuestionId : examQuestionIds) {
        if (gherkinScenarioRepository.existsByExamQuestion_ExamQuestionIdAndStatusTrue(examQuestionId)) {
            // Trả về thông báo lỗi thay vì ném ngoại lệ
            return ", Unsuccessfully, Gherkin has exits for Exam Question ID: " + examQuestionId;
        }
    }

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
            scenario.setStatus(true);
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

    @Override
    public List<GherkinScenarioDTO> getAllGherkinScenariosByExamQuestionId(Long examQuestionId) {
        if (examQuestionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam Question ID is required.");
        }

        // Lấy danh sách Gherkin_Scenario có status = true
        List<Gherkin_Scenario> scenarios = gherkinScenarioRepository
                .findByExamQuestion_ExamQuestionIdAndStatusTrueOrderByOrderPriorityAsc(examQuestionId);

        // Kiểm tra nếu không có dữ liệu
        if (scenarios.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No Gherkin Scenarios found for the provided Exam Question ID with status = true.");
        }

        // Chuyển đổi danh sách Gherkin_Scenario sang danh sách GherkinScenarioDTO
        return scenarios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Hàm helper để chuyển đổi từ Gherkin_Scenario sang GherkinScenarioDTO
    private GherkinScenarioDTO convertToDTO(Gherkin_Scenario scenario) {
        return new GherkinScenarioDTO(
                scenario.getGherkinScenarioId(),
                scenario.getGherkinData(),
                scenario.getOrderPriority(),
                scenario.getIsUpdateCreate(),
                scenario.getStatus(),
                scenario.getExamQuestion() != null ? scenario.getExamQuestion().getExamQuestionId() : null,
                scenario.getPostmanForGrading() != null ? scenario.getPostmanForGrading().getPostmanForGradingId()
                        : null);
    }

}
