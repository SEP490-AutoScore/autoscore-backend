package com.CodeEvalCrew.AutoScore.services.gherkin_scenario_service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CreateGherkinScenarioDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinPostmanPairDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinScenarioDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinScenarioResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Api_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Selected_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Prompt;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Purpose_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;
import com.CodeEvalCrew.AutoScore.models.Entity.Log;
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;
import com.CodeEvalCrew.AutoScore.repositories.account_selected_key_repository.AccountSelectedKeyRepository;
import com.CodeEvalCrew.AutoScore.repositories.aiprompt_repository.AIPromptRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.gherkin_scenario_repository.GherkinScenarioRepository;
import com.CodeEvalCrew.AutoScore.repositories.log_repository.LogRepository;
import com.CodeEvalCrew.AutoScore.repositories.postman_for_grading.PostmanForGradingRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class GherkinScenarioService implements IGherkinScenarioService {

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
        @Autowired
        private AccountSelectedKeyRepository accountSelectedKeyRepository;
        @Autowired
        private AIPromptRepository aiPromptRepository;
        @Autowired
        private LogRepository logRepository;
        @Autowired
        private IExamPaperRepository examPaperRepository;

        private void saveLog(Long examPaperId, String actionDetail) {

                Optional<Exam_Paper> optionalExamPaper = examPaperRepository.findById(examPaperId);
                if (optionalExamPaper.isEmpty()) {
                        throw new IllegalArgumentException("Exam Paper with ID " + examPaperId + " does not exist.");
                }

                Exam_Paper examPaper = optionalExamPaper.get();
                Log log = examPaper.getLog();

                if (log == null) {
                        log = new Log();
                        log.setExamPaper(examPaper);
                        log.setAllData(actionDetail);
                } else {

                        String updatedData = log.getAllData() == null ? "" : log.getAllData() + ", ";
                        log.setAllData(updatedData + actionDetail);
                }

                logRepository.save(log);
        }

        @Override
        public List<GherkinPostmanPairDTO> getAllGherkinAndPostmanPairsByQuestionId(Long questionId) {

                Exam_Question examQuestion = examQuestionRepository.findById(questionId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Exam question not found"));

                List<Gherkin_Scenario> gherkinScenarios = gherkinScenarioRepository
                                .findByExamQuestionAndStatusTrue(examQuestion);

                List<Postman_For_Grading> postmanForGradings = postmanForGradingRepository
                                .findByExamQuestionAndStatusTrueOrderByOrderPriorityAsc(examQuestion);

                List<GherkinPostmanPairDTO> pairs = new ArrayList<>();

                for (Postman_For_Grading postman : postmanForGradings) {
                        Gherkin_Scenario matchedGherkin = postman.getGherkinScenario();

                        if (matchedGherkin != null && matchedGherkin.isStatus()) {
                                GherkinDTO gherkinDTO = new GherkinDTO(
                                                matchedGherkin.getGherkinScenarioId(),
                                                matchedGherkin.getGherkinData(),
                                                matchedGherkin.isStatus(),
                                                matchedGherkin.getExamQuestion() != null
                                                                ? matchedGherkin.getExamQuestion().getExamQuestionId()
                                                                : null,
                                                postman.getPostmanForGradingId());

                                PostmanDTO postmanDTO = new PostmanDTO(
                                                postman.getPostmanForGradingId(),
                                                postman.getPostmanFunctionName(),
                                                postman.getScoreOfFunction(),
                                                postman.getScorePercentage(),
                                                postman.getTotalPmTest(),
                                                postman.isStatus(),
                                                postman.getOrderPriority(),
                                                postman.getPostmanForGradingParentId(),
                                                decodeFileCollectionPostman(postman.getFileCollectionPostman()), // Decode
                                                postman.getExamQuestion() != null
                                                                ? postman.getExamQuestion().getExamQuestionId()
                                                                : null,
                                                postman.getGherkinScenario() != null
                                                                ? postman.getGherkinScenario().getGherkinScenarioId()
                                                                : null,
                                                postman.getExamPaper() != null ? postman.getExamPaper().getExamPaperId()
                                                                : null);

                                pairs.add(new GherkinPostmanPairDTO(gherkinDTO, postmanDTO));
                        } else {

                                PostmanDTO postmanDTO = new PostmanDTO(
                                                postman.getPostmanForGradingId(),
                                                postman.getPostmanFunctionName(),
                                                postman.getScoreOfFunction(),
                                                postman.getScorePercentage(),
                                                postman.getTotalPmTest(),
                                                postman.isStatus(),
                                                postman.getOrderPriority(),
                                                postman.getPostmanForGradingParentId(),
                                                decodeFileCollectionPostman(postman.getFileCollectionPostman()),
                                                postman.getExamQuestion() != null
                                                                ? postman.getExamQuestion().getExamQuestionId()
                                                                : null,
                                                null,
                                                postman.getExamPaper() != null ? postman.getExamPaper().getExamPaperId()
                                                                : null);

                                pairs.add(new GherkinPostmanPairDTO(null, postmanDTO));
                        }
                }

                for (Gherkin_Scenario gherkin : gherkinScenarios) {
                        boolean isPaired = postmanForGradings.stream()
                                        .anyMatch(postman -> postman.getGherkinScenario() != null
                                                        && postman.getGherkinScenario().getGherkinScenarioId()
                                                                        .equals(gherkin.getGherkinScenarioId()));

                        if (!isPaired) {
                                GherkinDTO gherkinDTO = new GherkinDTO(
                                                gherkin.getGherkinScenarioId(),
                                                gherkin.getGherkinData(),
                                                gherkin.isStatus(),
                                                gherkin.getExamQuestion() != null
                                                                ? gherkin.getExamQuestion().getExamQuestionId()
                                                                : null,
                                                null);

                                pairs.add(new GherkinPostmanPairDTO(gherkinDTO, null));
                        }
                }

                return pairs;
        }

        private String decodeFileCollectionPostman(byte[] fileCollectionPostman) {
                if (fileCollectionPostman != null) {
                        return new String(fileCollectionPostman, StandardCharsets.UTF_8);
                }
                return null;
        }

        @Override
        public List<GherkinPostmanPairDTO> getAllGherkinAndPostmanPairs(Long examPaperId) {

                List<Exam_Question> examQuestions = examQuestionRepository.findByExamPaper_ExamPaperId(examPaperId);

                List<Postman_For_Grading> postmanForGradings = postmanForGradingRepository
                                .findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriorityAsc(examPaperId);

                List<GherkinPostmanPairDTO> pairs = new ArrayList<>();

                for (Postman_For_Grading postman : postmanForGradings) {
                        Gherkin_Scenario matchedGherkin = postman.getGherkinScenario();

                        if (matchedGherkin != null && matchedGherkin.isStatus()) {
                                GherkinDTO gherkinDTO = new GherkinDTO(
                                                matchedGherkin.getGherkinScenarioId(),
                                                matchedGherkin.getGherkinData(),
                                                matchedGherkin.isStatus(),
                                                matchedGherkin.getExamQuestion() != null
                                                                ? matchedGherkin.getExamQuestion().getExamQuestionId()
                                                                : null,
                                                postman.getPostmanForGradingId());

                                PostmanDTO postmanDTO = new PostmanDTO(
                                                postman.getPostmanForGradingId(),
                                                postman.getPostmanFunctionName(),
                                                postman.getScoreOfFunction(),
                                                postman.getScorePercentage(),
                                                postman.getTotalPmTest(),
                                                postman.isStatus(),
                                                postman.getOrderPriority(),
                                                postman.getPostmanForGradingParentId(),
                                                decodeFileCollectionPostman(postman.getFileCollectionPostman()), // Decode
                                                postman.getExamQuestion() != null
                                                                ? postman.getExamQuestion().getExamQuestionId()
                                                                : null,
                                                postman.getGherkinScenario() != null
                                                                ? postman.getGherkinScenario().getGherkinScenarioId()
                                                                : null,
                                                postman.getExamPaper() != null ? postman.getExamPaper().getExamPaperId()
                                                                : null);

                                pairs.add(new GherkinPostmanPairDTO(gherkinDTO, postmanDTO));
                        } else {

                                PostmanDTO postmanDTO = new PostmanDTO(
                                                postman.getPostmanForGradingId(),
                                                postman.getPostmanFunctionName(),
                                                postman.getScoreOfFunction(),
                                                postman.getScorePercentage(),
                                                postman.getTotalPmTest(),
                                                postman.isStatus(),
                                                postman.getOrderPriority(),
                                                postman.getPostmanForGradingParentId(),
                                                decodeFileCollectionPostman(postman.getFileCollectionPostman()),
                                                postman.getExamQuestion() != null
                                                                ? postman.getExamQuestion().getExamQuestionId()
                                                                : null,
                                                postman.getGherkinScenario() != null
                                                                ? postman.getGherkinScenario().getGherkinScenarioId()
                                                                : null,
                                                postman.getExamPaper() != null ? postman.getExamPaper().getExamPaperId()
                                                                : null);

                                pairs.add(new GherkinPostmanPairDTO(null, postmanDTO));
                        }
                }

                for (Exam_Question examQuestion : examQuestions) {
                        List<Gherkin_Scenario> gherkinScenarios = gherkinScenarioRepository
                                        .findByExamQuestionAndStatusTrue(examQuestion);

                        for (Gherkin_Scenario gherkin : gherkinScenarios) {
                                boolean isPaired = postmanForGradings.stream()
                                                .anyMatch(postman -> postman.getGherkinScenario() != null
                                                                && postman.getGherkinScenario().getGherkinScenarioId()
                                                                                .equals(gherkin.getGherkinScenarioId()));

                                if (!isPaired) {
                                        GherkinDTO gherkinDTO = new GherkinDTO(
                                                        gherkin.getGherkinScenarioId(),
                                                        gherkin.getGherkinData(),
                                                        gherkin.isStatus(),
                                                        examQuestion.getExamQuestionId(),
                                                        null);

                                        pairs.add(new GherkinPostmanPairDTO(gherkinDTO, null));
                                }
                        }
                }

                return pairs;
        }

        // @Override
        // public List<GherkinScenarioDTO> getAllGherkinScenariosByExamPaperId(Long examPaperId) {

        //         List<Gherkin_Scenario> scenarios = gherkinScenarioRepository
        //                         .findByExamQuestion_ExamPaper_ExamPaperIdAndStatusTrue(examPaperId);

        //         return scenarios.stream().map(scenario -> new GherkinScenarioDTO(
        //                         scenario.getGherkinScenarioId(),
        //                         scenario.getGherkinData(),
        //                         scenario.isStatus(),
        //                         scenario.getExamQuestion().getExamQuestionId(),
        //                         scenario.getPostmanForGrading() != null
        //                                         ? scenario.getPostmanForGrading().getPostmanForGradingId()
        //                                         : null))
        //                         .collect(Collectors.toList());
        // }

        @Override
        public ResponseEntity<String> generateGherkinFormat(Long examQuestionId) {

                Long authenticatedUserId = Util.getAuthenticatedAccountId();
                LocalDateTime time = Util.getCurrentDateTime();

                Exam_Question examQuestion = examQuestionRepository.findById(examQuestionId)
                                .orElseThrow(() -> new NoSuchElementException("Exam Question not exists"));
                Exam_Paper examPaper = examQuestion.getExamPaper();

                if (gherkinScenarioRepository.existsByExamQuestion_ExamQuestionIdAndStatusTrue(examQuestionId)) {
                        return ResponseEntity
                                        .status(HttpStatus.CONFLICT)
                                        .body("Unsuccessfully! Gherkin already exists for Exam Question ID: "
                                                        + examQuestionId);
                }

                Optional<Account_Selected_Key> optionalAccountSelectedKey = accountSelectedKeyRepository
                                .findByAccount_AccountId(authenticatedUserId);
                if (optionalAccountSelectedKey.isEmpty()) {
                        return ResponseEntity
                                        .status(HttpStatus.BAD_REQUEST)
                                        .body("User has not selected an AI Key");
                }

                Account_Selected_Key accountSelectedKey = optionalAccountSelectedKey.get();
                AI_Api_Key selectedAiApiKey = accountSelectedKey.getAiApiKey();
                if (selectedAiApiKey == null) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body("AI API Key not exists");
                }

                Optional<Exam_Database> optionalExamDatabase = examDatabaseRepository
                                .findByExamQuestionId(examQuestionId);
                if (optionalExamDatabase.isEmpty()) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body("Database not exists");
                }

                Exam_Database examDatabase = optionalExamDatabase.get();
                String databaseScript = examDatabase.getDatabaseScript();

                StringBuilder responseBuilder = new StringBuilder();

                List<AI_Prompt> orderedAIPrompts = aiPromptRepository
                                .findByPurposeOrderByOrderPriority(Purpose_Enum.GENERATE_GHERKIN_FORMAT);

                for (AI_Prompt aiprompt : orderedAIPrompts) {
                        String question = aiprompt.getQuestionAskAiContent();
                        if (aiprompt.getOrderPriority() == 1) {
                                question += "\n" + databaseScript;
                        } else if (aiprompt.getOrderPriority() == 2) {
                                question += ""
                                                + "\n - Question Content: " + examQuestion.getQuestionContent()
                                                + "\n - Allowed Roles: " + examQuestion.getRoleAllow()
                                                + "\n - Description: " + examQuestion.getDescription()
                                                + "\n - End point: " + examQuestion.getEndPoint()
                                                + "\n - Http method: " + examQuestion.getHttpMethod()
                                                + "\n - Payload type: " + examQuestion.getPayloadType()
                                                + "\n - Validation: " + examQuestion.getValidation()
                                                + "\n - Success response: " + examQuestion.getSucessResponse()
                                                + "\n - Error response: " + examQuestion.getErrorResponse()
                                                + "\n - Payload: " + examQuestion.getPayload();
                        }

                        String promptInUTF8 = new String(question.getBytes(StandardCharsets.UTF_8),
                                        StandardCharsets.UTF_8);
                        String response = sendToAI(promptInUTF8, selectedAiApiKey.getAiApiKey());

                        responseBuilder.append(response).append("\n");

                        if (aiprompt.getOrderPriority() == 2) {
                                List<String> gherkinDataList = extractGherkinData(response);
                                saveGherkinData(gherkinDataList, examQuestion);

                                saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                                                + "] [Generate gherkin successfully] at [" + time + "]");
                                return ResponseEntity
                                                .status(HttpStatus.OK)
                                                .body("Generate gherkin successfully!");
                        }

                }
                saveLog(examPaper.getExamPaperId(),
                                "Account [" + authenticatedUserId + "] [Generate gherkin failure] at [" + time + "]");

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Unknown error! Maybe AI did not respond");
        }

        public List<String> getGherkinDatasByExamQuestionId(Long examQuestionId) {

                Optional<Exam_Question> optionalExamQuestion = examQuestionRepository.findById(examQuestionId);
                if (optionalExamQuestion.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam Question not exists");
                }

                List<Gherkin_Scenario> gherkinScenarios = gherkinScenarioRepository
                                .findByExamQuestion_ExamQuestionIdAndStatusTrue(examQuestionId);

                // Trả về danh sách gherkinData
                return gherkinScenarios.stream()
                                .map(Gherkin_Scenario::getGherkinData)
                                .collect(Collectors.toList());
        }

        @Override
        public ResponseEntity<String> generateGherkinFormatMore(List<Long> gherkinIds, Long examQuestionId) {
                Long authenticatedUserId = Util.getAuthenticatedAccountId();
                LocalDateTime time = Util.getCurrentDateTime();

                Exam_Question examQuestion = examQuestionRepository.findById(examQuestionId)
                                .orElseThrow(() -> new NoSuchElementException("Exam Question not exists"));
                Exam_Paper examPaper = examQuestion.getExamPaper();

                Optional<Account_Selected_Key> optionalAccountSelectedKey = accountSelectedKeyRepository
                                .findByAccount_AccountId(authenticatedUserId);
                if (optionalAccountSelectedKey.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User has not selected an AI Key");
                }

                Account_Selected_Key accountSelectedKey = optionalAccountSelectedKey.get();
                AI_Api_Key selectedAiApiKey = accountSelectedKey.getAiApiKey();
                if (selectedAiApiKey == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("AI API Key does not exist");
                }

                List<Gherkin_Scenario> gherkinScenarios = gherkinScenarioRepository.findAllById(gherkinIds);

                if (gherkinScenarios.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body("No Gherkin data found for the provided Gherkin IDs");
                }

                // Long examQuestionId =
                // gherkinScenarios.get(0).getExamQuestion().getExamQuestionId();
                boolean allBelongToSameQuestion = gherkinScenarios.stream()
                                .allMatch(gherkin -> gherkin.getExamQuestion().getExamQuestionId()
                                                .equals(examQuestionId));

                if (!allBelongToSameQuestion) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("All Gherkin IDs must belong to the same Exam Question");
                }

                Optional<Exam_Database> optionalExamDatabase = examDatabaseRepository
                                .findByExamQuestionId(examQuestionId);
                if (optionalExamDatabase.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body("Database does not exist for Exam Question ID: " + examQuestionId);
                }

                Exam_Database examDatabase = optionalExamDatabase.get();
                String databaseScript = examDatabase.getDatabaseScript();

                Optional<Exam_Question> optionalExamQuestion = examQuestionRepository.findById(examQuestionId);
                if (optionalExamQuestion.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body("Exam Question does not exist for ID: " + examQuestionId);
                }

                // Exam_Question examQuestion = optionalExamQuestion.get();

                String formattedGherkinData = gherkinScenarios.stream()
                                .map(Gherkin_Scenario::getGherkinData)
                                .map(data -> "{{ " + data + " }}")
                                .collect(Collectors.joining("\n"));

                List<AI_Prompt> orderedAIPrompts = aiPromptRepository
                                .findByPurposeOrderByOrderPriority(Purpose_Enum.GENERATE_GHERKIN_FORMAT_MORE);

                for (AI_Prompt aiprompt : orderedAIPrompts) {
                        String question = aiprompt.getQuestionAskAiContent();

                        if (aiprompt.getOrderPriority() == 1) {
                                question += "\n" + databaseScript;
                        } else if (aiprompt.getOrderPriority() == 2) {
                                question += "\n" + formattedGherkinData
                                                + "\n - Question Content: " + examQuestion.getQuestionContent()
                                                + "\n - Allowed Roles: " + examQuestion.getRoleAllow()
                                                + "\n - Description: " + examQuestion.getDescription()
                                                + "\n - End point: " + examQuestion.getEndPoint()
                                                + "\n - Http method: " + examQuestion.getHttpMethod()
                                                + "\n - Payload type: " + examQuestion.getPayloadType()
                                                + "\n - Validation: " + examQuestion.getValidation()
                                                + "\n - Success response: " + examQuestion.getSucessResponse()
                                                + "\n - Error response: " + examQuestion.getErrorResponse()
                                                + "\n - Payload: " + examQuestion.getPayload();
                        }

                        String promptInUTF8 = new String(question.getBytes(StandardCharsets.UTF_8),
                                        StandardCharsets.UTF_8);
                        String response = sendToAI(promptInUTF8, selectedAiApiKey.getAiApiKey());

                        if (aiprompt.getOrderPriority() == 2) {

                                List<String> gherkinDataList = extractGherkinData(response);
                                saveGherkinData(gherkinDataList, examQuestion);

                                saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                                                + "] [Generate gherkin more successfully] at [" + time + "]");
                                return ResponseEntity.status(HttpStatus.OK).body("Generate gherkin more successfully!");
                        }
                }
                saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                                + "] [Generate gherkin more failure] at [" + time + "]");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Unknown error! AI may not have responded.");
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
                // long priority = 1;
                for (String data : gherkinDataList) {
                        Gherkin_Scenario scenario = new Gherkin_Scenario();
                        scenario.setGherkinData(data);
                        // scenario.setOrderPriority(priority++);
                        scenario.setExamQuestion(examQuestion);
                        scenario.setStatus(true);
                        // scenario.setIsUpdateCreate(true);

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
        public GherkinScenarioResponseDTO updateGherkinScenarios(Long gherkinScenarioId, String gherkinData) {

                Long authenticatedUserId = Util.getAuthenticatedAccountId();
                LocalDateTime time = Util.getCurrentDateTime();

                Optional<Gherkin_Scenario> optionalGherkinScenario = gherkinScenarioRepository
                                .findById(gherkinScenarioId);

                if (optionalGherkinScenario.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "Gherkin Scenario not found with ID: " + gherkinScenarioId);
                }

                Gherkin_Scenario gherkinScenario = optionalGherkinScenario.get();

                Exam_Question examQuestion = gherkinScenario.getExamQuestion();

                Exam_Paper examPaper = examQuestion.getExamPaper();

                if (examPaper == null) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "Exam Paper not found for the Gherkin Scenario");
                }

                gherkinScenario.setGherkinData(gherkinData);

                Gherkin_Scenario updatedGherkinScenario = gherkinScenarioRepository.save(gherkinScenario);
                saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                                + "] [Update Generate gherkin successfully] at [" + time + "]");

                GherkinScenarioResponseDTO responseDTO = new GherkinScenarioResponseDTO();
                responseDTO.setGherkinScenarioId(updatedGherkinScenario.getGherkinScenarioId());
                responseDTO.setGherkinData(updatedGherkinScenario.getGherkinData());
                responseDTO.setStatus(updatedGherkinScenario.isStatus());
                responseDTO.setExamQuestionId(updatedGherkinScenario.getExamQuestion() != null
                                ? updatedGherkinScenario.getExamQuestion().getExamQuestionId()
                                : null);
                responseDTO.setPostmanForGradingId(updatedGherkinScenario.getPostmanForGrading() != null
                                ? updatedGherkinScenario.getPostmanForGrading().getPostmanForGradingId()
                                : null);

                return responseDTO;
        }

        @Override
        public String deleteGherkinScenario(List<Long> gherkinScenarioIds, Long examPaperId) {

                Long authenticatedUserId = Util.getAuthenticatedAccountId();
                LocalDateTime time = Util.getCurrentDateTime();

                Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                                .orElseThrow(() -> new NoSuchElementException("Exam Paper not exists"));
             

                for (Long gherkinScenarioId : gherkinScenarioIds) {
                        Optional<Gherkin_Scenario> optionalGherkinScenario = gherkinScenarioRepository
                                        .findById(gherkinScenarioId);

                        if (optionalGherkinScenario.isEmpty()) {
                                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Gherkin Scenario not found with ID: " + gherkinScenarioId);
                        }

                        Gherkin_Scenario gherkinScenario = optionalGherkinScenario.get();

                        gherkinScenario.setStatus(false);
                        gherkinScenario.setPostmanForGrading(null);

                        gherkinScenarioRepository.save(gherkinScenario);
                }

                saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                                + "] [Delete gherkin successfully] at [" + time + "]");
                return "Successfully deleted Gherkin Scenarios with IDs: "
                                + String.join(", ", gherkinScenarioIds.stream().map(String::valueOf)
                                                .collect(Collectors.toList()));
        }

        @Override
        public GherkinScenarioResponseDTO createGherkinScenario(CreateGherkinScenarioDTO dto) {

                Long authenticatedUserId = Util.getAuthenticatedAccountId();
                LocalDateTime time = Util.getCurrentDateTime();

                Exam_Question examQuestion = examQuestionRepository.findById(dto.getExamQuestionId())
                                .orElseThrow(() -> new NoSuchElementException("Exam Question not exists"));
                Exam_Paper examPaper = examQuestion.getExamPaper();

                Optional<Exam_Question> optionalExamQuestion = examQuestionRepository.findById(dto.getExamQuestionId());

                if (optionalExamQuestion.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "Exam Question not found with ID: " + dto.getExamQuestionId());
                }

                // Exam_Question examQuestion = optionalExamQuestion.get();

                Gherkin_Scenario gherkinScenario = new Gherkin_Scenario();
                gherkinScenario.setGherkinData(dto.getGherkinData());
                gherkinScenario.setStatus(true);
                gherkinScenario.setExamQuestion(examQuestion);
                gherkinScenario.setPostmanForGrading(null);

                Gherkin_Scenario savedGherkinScenario = gherkinScenarioRepository.save(gherkinScenario);
                saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                                + "] [Create gherkin successfully] at [" + time + "]");

                GherkinScenarioResponseDTO responseDTO = new GherkinScenarioResponseDTO();
                responseDTO.setGherkinScenarioId(savedGherkinScenario.getGherkinScenarioId());
                responseDTO.setGherkinData(savedGherkinScenario.getGherkinData());
                responseDTO.setStatus(savedGherkinScenario.isStatus());
                responseDTO.setExamQuestionId(savedGherkinScenario.getExamQuestion().getExamQuestionId());
                responseDTO.setPostmanForGradingId(savedGherkinScenario.getPostmanForGrading() != null
                                ? savedGherkinScenario.getPostmanForGrading().getPostmanForGradingId()
                                : null);

                return responseDTO;
        }

        @Override
        public GherkinScenarioDTO getById(Long gherkinScenarioId) {

                Optional<Gherkin_Scenario> optionalGherkinScenario = gherkinScenarioRepository
                                .findById(gherkinScenarioId);
                if (optionalGherkinScenario.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "Gherkin_Scenario not found with ID: " + gherkinScenarioId);
                }

                Gherkin_Scenario gherkinScenario = optionalGherkinScenario.get();

                GherkinScenarioDTO dto = new GherkinScenarioDTO();
                dto.setGherkinScenarioId(gherkinScenario.getGherkinScenarioId());
                dto.setGherkinData(gherkinScenario.getGherkinData());
                dto.setStatus(gherkinScenario.isStatus());

                dto.setExamQuestionId(
                                gherkinScenario.getExamQuestion() != null
                                                ? gherkinScenario.getExamQuestion().getExamQuestionId()
                                                : null);

                dto.setPostmanForGradingId(gherkinScenario.getPostmanForGrading() != null
                                ? gherkinScenario.getPostmanForGrading().getPostmanForGradingId()
                                : null);

                return dto;
        }
}
