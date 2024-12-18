package com.CodeEvalCrew.AutoScore.services.postman_for_grading_service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.persistence.exceptions.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingGetbyIdDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Api_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Prompt;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Selected_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.GradingStatusEnum;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Purpose_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;
import com.CodeEvalCrew.AutoScore.models.Entity.GradingProcess;
import com.CodeEvalCrew.AutoScore.models.Entity.Log;
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;
import com.CodeEvalCrew.AutoScore.repositories.account_selected_key_repository.AccountSelectedKeyRepository;
import com.CodeEvalCrew.AutoScore.repositories.aiprompt_repository.AIPromptRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.gherkin_scenario_repository.GherkinScenarioRepository;
import com.CodeEvalCrew.AutoScore.repositories.grading_process_repository.GradingProcessRepository;
import com.CodeEvalCrew.AutoScore.repositories.log_repository.LogRepository;
import com.CodeEvalCrew.AutoScore.repositories.postman_for_grading.PostmanForGradingRepository;
import com.CodeEvalCrew.AutoScore.utils.PathUtil;
import com.CodeEvalCrew.AutoScore.utils.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class PostmanForGradingService implements IPostmanForGradingService {

    @Autowired
    private PostmanForGradingRepository postmanForGradingRepository;
    @Autowired
    private GherkinScenarioRepository gherkinScenarioRepository;
    @Autowired
    private IExamDatabaseRepository examDatabaseRepository;
    @Autowired
    private IExamPaperRepository examPaperRepository;
    @Autowired
    private AccountSelectedKeyRepository accountSelectedKeyRepository;
    @Autowired
    private AIPromptRepository aiPromptRepository;
    @Autowired
    private IExamQuestionRepository examQuestionRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private PathUtil pathUtil;
    @Autowired
    private GradingProcessRepository gradingProcessRepository;

    private Long totalPmTest;

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

    private void checkGradingProcessStatus(Long examPaperId) throws IllegalStateException {
        Optional<GradingProcess> gradingProcessOpt = gradingProcessRepository.findByExamPaper_ExamPaperId(examPaperId);

        if (gradingProcessOpt.isPresent()) {
            GradingProcess gradingProcess = gradingProcessOpt.get();
            GradingStatusEnum status = gradingProcess.getStatus();

            if (status == GradingStatusEnum.PENDING ||
                    status == GradingStatusEnum.IMPORTANT ||
                    status == GradingStatusEnum.GRADING ||
                    status == GradingStatusEnum.PLAGIARISM) {
                throw new IllegalStateException(
                        String.format("Grading processing, not allowed",
                                examPaperId, status));
            }
        }
    }

    private boolean isFirstElementValid(PostmanForGradingUpdateDTO firstElement) {

        return firstElement != null
                && firstElement.getPostmanForGradingId() == 0
                && "Root of tree".equals(firstElement.getPostmanFunctionName());

    }

    @Override
    public String updatePostmanForGrading(Long examPaperId, List<PostmanForGradingUpdateDTO> updateDTOs) {
        checkGradingProcessStatus(examPaperId);
        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        if (updateDTOs == null || updateDTOs.isEmpty()) {
            throw new IllegalArgumentException("The list of updateDTOs cannot be empty!");
        }

        if (updateDTOs.isEmpty() || !isFirstElementValid(updateDTOs.get(0))) {
            return "The first element of updateDTOs is invalid.";
        }

        int orderPriority = 1;
        for (int i = 1; i < updateDTOs.size(); i++) {
            PostmanForGradingUpdateDTO dto = updateDTOs.get(i);

            if (dto.getPostmanForGradingId() == 0) {
                throw new RuntimeException(
                        "List element is invalid: postmanForGradingId cannot be 0 other than the first element.");
            }

            Postman_For_Grading postman = postmanForGradingRepository.findById(dto.getPostmanForGradingId())
                    .orElseThrow(() -> new RuntimeException(
                            "Postman_For_Grading with ID not found: " + dto.getPostmanForGradingId()));

            postman.setPostmanForGradingParentId(dto.getPostmanForGradingParentId());

            if (dto.getPostmanForGradingParentId() == 0) {
                postman.setPostmanForGradingParentId(null);
            }

            postman.setOrderPriority((long) orderPriority);

            orderPriority++;

            postmanForGradingRepository.save(postman);

        }

        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new RuntimeException("Exam_Paper not found with ID:" + examPaperId));

        String jsonFile;

        try {
            jsonFile = new String(examPaper.getFileCollectionPostman(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Cannot read fileCollectionPostman", e);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;

        try {
            rootNode = objectMapper.readTree(jsonFile);
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse JSON from fileCollectionPostman", e);
        }

        JsonNode itemsNode = rootNode.path("item");
        if (!itemsNode.isArray()) {
            throw new RuntimeException("Invalid JSON structure: missing field 'item' or not an array");
        }

        List<JsonNode> itemsList = new ArrayList<>();
        itemsNode.forEach(itemsList::add);

        List<String> orderedNames = updateDTOs.stream()
                .map(PostmanForGradingUpdateDTO::getPostmanFunctionName)
                .collect(Collectors.toList());

        itemsList.sort(Comparator.comparingInt(item -> {
            String name = item.path("name").asText();
            return orderedNames.indexOf(name);
        }));

        ArrayNode sortedItemsNode = objectMapper.createArrayNode();
        itemsList.forEach(sortedItemsNode::add);
        ((ObjectNode) rootNode).set("item", sortedItemsNode);

        try {
            examPaper.setFileCollectionPostman(objectMapper.writeValueAsBytes(rootNode));
        } catch (Exception e) {
            throw new RuntimeException("Unable to update fileCollectionPostman", e);
        }

        examPaper.setIsComfirmFile(false);
        examPaperRepository.save(examPaper);

        saveLog(examPaper.getExamPaperId(),
                "Account [" + authenticatedUserId + "] [Update function tree successfully] at [" + time + "]");
        return "Successfully";
    }

    @Override
    public String mergePostmanCollections(Long examPaperId) {
        checkGradingProcessStatus(examPaperId);
        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        try {
            List<Postman_For_Grading> postmanList = postmanForGradingRepository
                    .findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriorityAsc(examPaperId);

            if (postmanList.isEmpty()) {
                return "No Postman Collection files found for Exam Paper ID:" + examPaperId;
            }

            JSONObject mergedCollection = new JSONObject();
            JSONArray mergedItems = new JSONArray();

            Exam_Paper examPaper = examPaperRepository.findById(examPaperId).orElseThrow(
                    () -> new RuntimeException("Exam Paper not existed ID: " + examPaperId));

            String examCode = (examPaper.getExam() != null) ? examPaper.getExam().getExamCode() : "Unknown";

            JSONObject info = new JSONObject();
            info.put("_postman_id", java.util.UUID.randomUUID().toString());
            info.put("name", examCode + "-" + examPaper.getExamPaperCode() + "-postman-collection");
            info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
            info.put("_exporter_id", "29781870");

            mergedCollection.put("info", info);

            for (Postman_For_Grading postman : postmanList) {
                String fileContent = new String(postman.getFileCollectionPostman(), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(fileContent);

                if (jsonObject.has("item")) {
                    JSONArray items = jsonObject.getJSONArray("item");
                    for (int i = 0; i < items.length(); i++) {
                        mergedItems.put(items.getJSONObject(i));
                    }
                }
            }

            mergedCollection.put("item", mergedItems);

            byte[] mergedFileContent = mergedCollection.toString().getBytes(StandardCharsets.UTF_8);
            examPaper.setFileCollectionPostman(mergedFileContent);
            examPaper.setIsComfirmFile(false);
            examPaperRepository.save(examPaper);

            saveLog(examPaper.getExamPaperId(),
                    "Account [" + authenticatedUserId + "] [Merge all file postman collection successfully] at [" + time
                            + "]");
            return "Successfully merged Postman Collections for examPaperId " + examPaperId;

        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return "An error occurred when merging the Postman Collection file: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "An unknown error occurred: " + e.getMessage();
        }
    }

    @Override
    public ResponseEntity<?> generatePostmanCollection(Long gherkinScenarioId) {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        Gherkin_Scenario gherkinScenario = gherkinScenarioRepository.findById(gherkinScenarioId).orElse(null);
        if (gherkinScenario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Gherkin Scenario ID not found");
        }

        Exam_Question examQuestion = gherkinScenario.getExamQuestion();
        Exam_Paper examPaper = examQuestion.getExamPaper();

        checkGradingProcessStatus(examPaper.getExamPaperId());

        Optional<Postman_For_Grading> existingPostman = postmanForGradingRepository
                .findByGherkinScenario_GherkinScenarioIdAndStatusTrue(gherkinScenarioId);
        if (existingPostman.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Active Postman collection already exists for this Gherkin Scenario ID.");
        }

        Exam_Database examDatabase = examDatabaseRepository
                .findByExamPaper_ExamPaperId(examPaper.getExamPaperId())
                .orElse(null);
        if (examDatabase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Exam Database not found for the associated Exam Paper");
        }

        Account_Selected_Key accountSelectedKey = accountSelectedKeyRepository
                .findByAccount_AccountId(authenticatedUserId)
                .orElse(null);
        if (accountSelectedKey == null || accountSelectedKey.getAiApiKey() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User has not selected a valid AI Key");
        }

        AI_Api_Key selectedAiApiKey = accountSelectedKey.getAiApiKey();

        List<AI_Prompt> orderedAIPrompts = aiPromptRepository
                .findByPurposeOrderByOrderPriority(Purpose_Enum.GENERATE_POSTMAN_COLLECTION);
        if (orderedAIPrompts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No AI Prompts found.");
        }

        StringBuilder fullResponseBuilder = new StringBuilder();

        AI_Prompt firstPrompt = orderedAIPrompts.get(0);
        String firstQuestion = firstPrompt.getQuestionAskAiContent() + "\nDatabase Script: "
                + examDatabase.getDatabaseScript();

        String firstResponse = sendToAI(firstQuestion, selectedAiApiKey.getAiApiKey());
        if (firstResponse == null || firstResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to call AI API for orderPriority 1.");
        }

        fullResponseBuilder.append(firstResponse).append("\n");

        if (orderedAIPrompts.size() > 1) {
            AI_Prompt secondPrompt = orderedAIPrompts.get(1);
            String secondQuestion = secondPrompt.getQuestionAskAiContent()
                    + "\n" + gherkinScenario.getGherkinData()
                    + "\n\n"
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

            String secondQuestionUtf8 = new String(secondQuestion.getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8);
            String secondResponse = sendToAI(secondQuestionUtf8, selectedAiApiKey.getAiApiKey());

            if (secondResponse == null || secondResponse.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: Failed to call AI API for orderPriority 2.");
            }

            fullResponseBuilder.append(secondResponse).append("\n");

            String collectionJson = extractJsonFromResponse(secondResponse);
            if (collectionJson == null || collectionJson.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: JSON not found in the AI response for orderPriority 2.");
            }

            String postmanFunctionName = runNewman(collectionJson);
            if (postmanFunctionName == null) {
                throw new IllegalStateException("Error: Newman execution failed or postmanFunctionName not found.");
            }

            Postman_For_Grading postmanForGrading = new Postman_For_Grading();
            postmanForGrading.setGherkinScenario(gherkinScenario);
            postmanForGrading.setExamQuestion(examQuestion);
            postmanForGrading.setExamPaper(examPaper);
            postmanForGrading.setFileCollectionPostman(collectionJson.getBytes(StandardCharsets.UTF_8));
            postmanForGrading.setPostmanFunctionName(postmanFunctionName);
            postmanForGrading.setTotalPmTest(totalPmTest);
            postmanForGrading.setStatus(true);
            postmanForGradingRepository.save(postmanForGrading);

            saveLog(examPaper.getExamPaperId(),
                    "Account [" + authenticatedUserId + "] [Generate postman script successfully] at [" + time + "]");
            return ResponseEntity.status(HttpStatus.OK).body("Postman Collection generated successfully!");
        }

        saveLog(examPaper.getExamPaperId(),
                "Account [" + authenticatedUserId + "] [Generate postman script failure] at [" + time + "]");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unknown error! AI may not have responded.");
    }

    @Override
    public ResponseEntity<?> generatePostmanCollectionMore(Long postmanForGradingId) {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        Postman_For_Grading postmanForGrading = postmanForGradingRepository.findById(postmanForGradingId)
                .orElse(null);
        if (postmanForGrading == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Postman For Grading ID not found");
        }

        checkGradingProcessStatus(postmanForGrading.getExamPaper().getExamPaperId());

        Exam_Database examDatabase = examDatabaseRepository
                .findByExamPaper_ExamPaperId(postmanForGrading.getExamQuestion().getExamPaper().getExamPaperId())
                .orElse(null);
        if (examDatabase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Exam Database not found for the associated Exam Paper");
        }

        List<AI_Prompt> orderedAIPrompts = aiPromptRepository
                .findByPurposeOrderByOrderPriority(Purpose_Enum.GENERATE_POSTMAN_COLLECTION_MORE);
        if (orderedAIPrompts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No AI Prompts found.");
        }

        StringBuilder fullResponseBuilder = new StringBuilder();

        AI_Prompt firstPrompt = orderedAIPrompts.get(0);
        String firstQuestion = firstPrompt.getQuestionAskAiContent() + "\nDatabase Script: "
                + examDatabase.getDatabaseScript();

        String firstResponse = sendToAI(firstQuestion, getAuthenticatedUserApiKey(authenticatedUserId));
        if (firstResponse == null || firstResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Failed to call AI API for orderPriority 1.");
        }

        fullResponseBuilder.append(firstResponse).append("\n");

        if (orderedAIPrompts.size() > 1) {
            AI_Prompt secondPrompt = orderedAIPrompts.get(1);
            String secondQuestion = secondPrompt.getQuestionAskAiContent() + buildQuestionDetails(postmanForGrading);

            String secondQuestionUtf8 = new String(secondQuestion.getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8);

            String secondResponse = sendToAI(secondQuestionUtf8, getAuthenticatedUserApiKey(authenticatedUserId));
            if (secondResponse == null || secondResponse.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: Failed to call AI API for orderPriority 2.");
            }

            fullResponseBuilder.append(secondResponse).append("\n");

            String collectionJson = extractJsonFromResponse(secondResponse);
            if (collectionJson == null || collectionJson.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: JSON not found in the AI response for orderPriority 2.");
            }

            String postmanFunctionName = runNewman(collectionJson);
            if (postmanFunctionName == null) {
                throw new IllegalStateException("Error: Newman execution failed or postmanFunctionName not found.");
            }

            postmanForGrading.setFileCollectionPostman(collectionJson.getBytes(StandardCharsets.UTF_8));
            postmanForGrading.setPostmanFunctionName(postmanFunctionName);
            postmanForGrading.setTotalPmTest(totalPmTest);
            postmanForGrading.setStatus(true);
            postmanForGradingRepository.save(postmanForGrading);

            saveLog(postmanForGrading.getExamPaper().getExamPaperId(),
                    "Account [" + authenticatedUserId + "] [Generate Postman script more successfully] at [" + time
                            + "]");
            return ResponseEntity.status(HttpStatus.OK).body("Postman Collection generated more successfully!");
        }

        saveLog(postmanForGrading.getExamPaper().getExamPaperId(),
                "Account [" + authenticatedUserId + "] [Generate Postman script failure] at [" + time + "]");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unknown error! Possibly no response from AI.");
    }

    // Helper methods
    private String getAuthenticatedUserApiKey(Long authenticatedUserId) {
        return accountSelectedKeyRepository.findByAccount_AccountId(authenticatedUserId)
                .orElseThrow(() -> new NoSuchElementException("User AI Key not found"))
                .getAiApiKey().getAiApiKey();
    }

    private String buildQuestionDetails(Postman_For_Grading postmanForGrading) {
        return "\n\n\n"
                + "\n - Question Content: " + postmanForGrading.getExamQuestion().getQuestionContent()
                + "\n - Allowed Roles: " + postmanForGrading.getExamQuestion().getRoleAllow()
                + "\n - Description: " + postmanForGrading.getExamQuestion().getDescription()
                + "\n - End point: " + postmanForGrading.getExamQuestion().getEndPoint()
                + "\n - Http method: " + postmanForGrading.getExamQuestion().getHttpMethod()
                + "\n - Payload type: " + postmanForGrading.getExamQuestion().getPayloadType()
                + "\n - Validation: " + postmanForGrading.getExamQuestion().getValidation()
                + "\n - Success response: " + postmanForGrading.getExamQuestion().getSucessResponse()
                + "\n - Error response: " + postmanForGrading.getExamQuestion().getErrorResponse()
                + "\n - Payload: " + postmanForGrading.getExamQuestion().getPayload()
                + "\n - Gherkin Data: "
                + (postmanForGrading.getGherkinScenario() != null
                        && postmanForGrading.getGherkinScenario().getGherkinData() != null
                                ? postmanForGrading.getGherkinScenario().getGherkinData()
                                : "N/A")
                + "\n " + new String(postmanForGrading.getFileCollectionPostman(), StandardCharsets.UTF_8);
    }

    private String extractJsonFromResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            if (!jsonResponse.has("candidates")) {
                throw new JSONException("Missing 'candidates' field in response.");
            }
            String jsonString = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            jsonString = jsonString.replaceAll("(?s)^```json\\n", "").replaceAll("(?s)```$", "").trim();
            int startIndex = jsonString.indexOf("{");
            int endIndex = jsonString.lastIndexOf("}");
            if (startIndex != -1 && endIndex != -1) {
                jsonString = jsonString.substring(startIndex, endIndex + 1);
            }
            try {
                new JSONObject(jsonString);
                JSONObject parsedJson = new JSONObject(jsonString);
                if (parsedJson.has("item")) {
                    JSONArray items = parsedJson.getJSONArray("item");
                    if (items.length() > 1) {
                        throw new JSONException("The JSON contains more than one item.");
                    }
                }
                return jsonString;
            } catch (JSONException e) {
                System.err.println("Invalid JSON format: " + e.getMessage());
                return null;
            }

        } catch (JSONException e) {
            System.err.println("Error extracting JSON from response: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String runNewman(String collectionJson) {
        String postmanFunctionName = null;
        totalPmTest = 0L;
        Path tempFile = null;
        String newmanCmdPath = pathUtil.getNewmanCmdPath();

        try {
            String randomFileName = generateRandomFileName();
            tempFile = Files.createTempFile(randomFileName, ".json");
            Files.write(tempFile, collectionJson.getBytes(StandardCharsets.UTF_8));

            ProcessBuilder processBuilder = new ProcessBuilder(
                    newmanCmdPath,
                    "run",
                    tempFile.toAbsolutePath().toString());

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            StringBuilder outputBuilder = new StringBuilder();
            boolean assertionsFound = false;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line).append("\n");
                if (line.contains("→")) {
                    postmanFunctionName = line.substring(line.indexOf("→") + 1).trim();
                }
                if (line.contains("│              assertions │")) {
                    try {
                        String[] parts = line.trim().split("│");
                        if (parts.length > 2) {
                            totalPmTest = Long.parseLong(parts[2].trim());
                            assertionsFound = true;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error converting number of assertions: " + e.getMessage());
                        totalPmTest = 0L;
                    }
                }
            }

            int exitCode = process.waitFor();

            if (!assertionsFound) {
                throw new IllegalStateException("Assertions information not found in Newman output.");
            }

            if (totalPmTest == 0) {
                throw new IllegalStateException("No tests found in Newman output.");
            }

            if (exitCode == 0 || outputBuilder.toString().contains("executed")) {
                return postmanFunctionName;
            } else {
                throw new IllegalStateException("Newman run failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error running Newman: " + e.getMessage(), e);
        } finally {
            try {
                // Delete the temp file after use if it exists
                if (tempFile != null) {
                    Files.deleteIfExists(tempFile);
                }
            } catch (IOException e) {
                System.err.println("Failed to delete temp file: " + e.getMessage());
            }
        }
    }

    private String generateRandomFileName() {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomName = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            int index = (int) (Math.random() * characters.length());
            randomName.append(characters.charAt(index));
        }
        return randomName.toString();
    }

    private long countTestCases(String output) {
        long count = 0;
        try (BufferedReader reader = new BufferedReader(new StringReader(output))) {
            String line;
            while ((line = reader.readLine()) != null) {

                if (line.trim().matches("^\\d+\\..*")) {
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error when counting test cases: " + e.getMessage());
        }
        return count;
    }

    private String sendToAI(String prompt, String aiApiKey) {
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

        String responseBody = response.getBody();
        System.out.println("Response Body: " + responseBody);

        return responseBody;
    }

    @Override
    public List<PostmanForGradingDTO> getPostmanForGradingByExamPaperId(Long examPaperId) {

        List<Postman_For_Grading> postmanForGradingEntries = postmanForGradingRepository
                .findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriorityAsc(examPaperId);

        List<PostmanForGradingDTO> dtoList = postmanForGradingEntries.stream()
                .map(entry -> {
                    PostmanForGradingDTO dto = new PostmanForGradingDTO();
                    dto.setPostmanForGradingId(entry.getPostmanForGradingId());
                    dto.setPostmanFunctionName(entry.getPostmanFunctionName());
                    dto.setScoreOfFunction(entry.getScoreOfFunction());
                    dto.setScorePercentage(entry.getScorePercentage());
                    dto.setTotalPmTest(entry.getTotalPmTest());
                    dto.setOrderPriority(entry.getOrderPriority());
                    dto.setStatus(entry.isStatus());

                    dto.setPostmanForGradingParentId(
                            entry.getPostmanForGradingParentId() != null
                                    ? entry.getPostmanForGradingParentId()
                                    : 0L);
                    dto.setExamQuestionId(entry.getExamQuestion() != null
                            ? entry.getExamQuestion().getExamQuestionId()
                            : null);
                    dto.setGherkinScenarioId(entry.getGherkinScenario() != null
                            ? entry.getGherkinScenario().getGherkinScenarioId()
                            : null);
                    dto.setEndPoint(entry.getExamQuestion() != null
                            ? entry.getExamQuestion().getEndPoint()
                            : null);
                    return dto;
                })
                .collect(Collectors.toList());

        PostmanForGradingDTO rootElement = new PostmanForGradingDTO();
        rootElement.setPostmanForGradingId(0L);
        rootElement.setPostmanFunctionName("Root of tree");
        rootElement.setScoreOfFunction(0F);
        rootElement.setScorePercentage(0F);
        rootElement.setTotalPmTest(null);
        rootElement.setOrderPriority(0L);
        rootElement.setStatus(true);
        rootElement.setPostmanForGradingParentId(0L);
        rootElement.setExamQuestionId(null);
        rootElement.setGherkinScenarioId(null);
        rootElement.setEndPoint(null);

        List<PostmanForGradingDTO> updatedList = new ArrayList<>();
        updatedList.add(rootElement);
        updatedList.addAll(dtoList);

        Map<Long, PostmanForGradingDTO> dtoMap = updatedList.stream()
                .collect(Collectors.toMap(PostmanForGradingDTO::getPostmanForGradingId, dto -> dto));

        updatedList.forEach(dto -> {
            Long parentId = dto.getPostmanForGradingParentId();
            if (parentId != null && parentId != 0L && !dtoMap.containsKey(parentId)) {
                dto.setPostmanForGradingParentId(0L);
            }
        });

        Set<Long> visited = new HashSet<>();
        Set<Long> inRecStack = new HashSet<>();
        List<Long> cyclicIds = new ArrayList<>();

        for (PostmanForGradingDTO dto : updatedList) {
            if (!visited.contains(dto.getPostmanForGradingId())) {
                detectCycle(dto.getPostmanForGradingId(), dtoMap, visited, inRecStack, cyclicIds);
            }
        }

        if (!cyclicIds.isEmpty()) {
            updatedList.forEach(dto -> {
                if (cyclicIds.contains(dto.getPostmanForGradingId())) {
                    dto.setPostmanForGradingParentId(0L);
                }
            });
        }
        return updatedList;
    }

    private void detectCycle(Long nodeId, Map<Long, PostmanForGradingDTO> dtoMap, Set<Long> visited,
            Set<Long> inRecStack, List<Long> cyclicIds) {
        visited.add(nodeId);
        inRecStack.add(nodeId);
        PostmanForGradingDTO currentNode = dtoMap.get(nodeId);
        if (currentNode != null) {
            Long parentId = currentNode.getPostmanForGradingParentId();
            if (dtoMap.containsKey(parentId)) {
                if (!visited.contains(parentId)) {
                    detectCycle(parentId, dtoMap, visited, inRecStack, cyclicIds);
                } else if (inRecStack.contains(parentId)) {
                    cyclicIds.add(nodeId);
                    cyclicIds.add(parentId);
                }
            }
        }

        inRecStack.remove(nodeId);
    }

    @Override
    public String deletePostmanForGrading(List<Long> postmanForGradingIds, Long examPaperId) {
        checkGradingProcessStatus(examPaperId);
        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();
        Exam_Paper examPaper2 = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new NoSuchElementException("Exam Paper not exists"));
        StringBuilder response = new StringBuilder();
        for (Long postmanForGradingId : postmanForGradingIds) {
            Optional<Postman_For_Grading> optionalPostman = postmanForGradingRepository.findById(postmanForGradingId);
            if (optionalPostman.isEmpty()) {
                response.append("Postman_For_Grading not found with ID: " + postmanForGradingId + "\n");
                continue;
            }
            Postman_For_Grading postman = optionalPostman.get();
            Exam_Paper examPaper = postman.getExamPaper();
            if (examPaper != null) {
                examPaper.setIsComfirmFile(false);
                examPaperRepository.save(examPaper);
            }
            postman.setStatus(false);
            postman.setPostmanFunctionName(null);
            postman.setExamQuestion(null);
            postman.setGherkinScenario(null);
            postmanForGradingRepository.save(postman);
            response.append(
                    "Postman_For_Grading with ID: " + postmanForGradingId + " has been successfully deleted.\n");
        }
        saveLog(examPaper2.getExamPaperId(),
                "Account [" + authenticatedUserId + "] [Delete postman script successfully] at [" + time + "]");
        return response.toString();
    }

    @Override
    public ResponseEntity<PostmanForGradingGetbyIdDTO> getPostmanForGradingById(Long id) {
        Optional<Postman_For_Grading> optionalPostmanForGrading = postmanForGradingRepository.findById(id);
        if (optionalPostmanForGrading.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Postman_For_Grading postmanForGrading = optionalPostmanForGrading.get();
        PostmanForGradingGetbyIdDTO dto = new PostmanForGradingGetbyIdDTO();
        dto.setPostmanForGradingId(postmanForGrading.getPostmanForGradingId());
        dto.setPostmanFunctionName(postmanForGrading.getPostmanFunctionName());
        dto.setScoreOfFunction(postmanForGrading.getScoreOfFunction());
        dto.setTotalPmTest(postmanForGrading.getTotalPmTest());
        dto.setStatus(postmanForGrading.isStatus());
        dto.setOrderPriority(postmanForGrading.getOrderPriority());
        dto.setPostmanForGradingParentId(postmanForGrading.getPostmanForGradingParentId());
        if (postmanForGrading.getExamQuestion() != null) {
            dto.setExamQuestionId(postmanForGrading.getExamQuestion().getExamQuestionId());
        }
        if (postmanForGrading.getGherkinScenario() != null) {
            dto.setGherkinScenarioId(postmanForGrading.getGherkinScenario().getGherkinScenarioId());
        }
        dto.setExamPaperId(postmanForGrading.getExamPaper().getExamPaperId());
        if (postmanForGrading.getFileCollectionPostman() != null) {
            String fileCollectionJson = new String(postmanForGrading.getFileCollectionPostman(),
                    StandardCharsets.UTF_8);
            dto.setFileCollectionPostman(fileCollectionJson);
        }
        return ResponseEntity.ok(dto);
    }

    @Override
    public String updateExamQuestionId(Long postmanForGradingId, Long examQuestionId) {
        Exam_Question examQuestion = examQuestionRepository.findById(examQuestionId)
                .orElseThrow(() -> new NoSuchElementException("Exam Question not exists"));
        Exam_Paper examPaper = examQuestion.getExamPaper();
        checkGradingProcessStatus(examPaper.getExamPaperId());

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        Optional<Postman_For_Grading> optionalPostmanForGrading = postmanForGradingRepository
                .findById(postmanForGradingId);
        if (optionalPostmanForGrading.isEmpty()) {
            return "PostmanForGrading does not exist with ID: " + postmanForGradingId;
        }
        Postman_For_Grading postmanForGrading = optionalPostmanForGrading.get();
        Optional<Exam_Question> optionalExamQuestion = examQuestionRepository.findById(examQuestionId);
        if (optionalExamQuestion.isEmpty()) {
            return "ExamQuestion does not exist with ID: " + examQuestionId;
        }
        postmanForGrading.setExamQuestion(examQuestion);
        postmanForGradingRepository.save(postmanForGrading);
        saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                + "] [Update question of postman script successfully] at [" + time + "]");
        return "Successfully updated PostmanForGrading with ID: " + postmanForGradingId + " and ExamQuestion ID: "
                + examQuestionId;
    }

    @Override
    public void calculateScores(Long examPaperId) throws Exception {
        checkGradingProcessStatus(examPaperId);
        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        // Get Exam_Paper by ID
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new Exception("Exam Paper not found"));

        // Calculate the total score of Exam_Paper
        float totalExamPaperScore = examPaper.getExamQuestions().stream()
                .filter(Objects::nonNull)
                .map(Exam_Question::getExamQuestionScore)
                .reduce(0f, Float::sum);

        System.out.println("Total Exam Paper Score: " + totalExamPaperScore);

        // Get all Postman_For_Grading by examPaperId
        List<Postman_For_Grading> postmanList = postmanForGradingRepository
                .findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriorityAsc(examPaperId);

        for (Postman_For_Grading postman : postmanList) {
            if (postman.getExamQuestion() == null) {
                throw new Exception(
                        "Postman_For_Grading ID " + postman.getPostmanForGradingId() + " has null examQuestionId.");
            }
        }

        // Group Postman_For_Grading by Exam_Question
        Map<Exam_Question, List<Postman_For_Grading>> groupedByQuestion = postmanList.stream()
                .collect(Collectors.groupingBy(Postman_For_Grading::getExamQuestion));

        for (Exam_Question question : examPaper.getExamQuestions()) {
            if (question == null)
                continue;

            List<Postman_For_Grading> functionsForQuestion = groupedByQuestion.getOrDefault(question,
                    new ArrayList<>());

            // Filter functions containing "success" or "successfully"
            boolean hasSuccessFunction = functionsForQuestion.stream()
                    .anyMatch(f -> {
                        String functionName = f.getPostmanFunctionName().toLowerCase();
                        String[] words = functionName.split("\\s+");
                        return Arrays.stream(words)
                                .anyMatch(word -> word.equals("success") || word.equals("successfully"));
                    });

            if (!hasSuccessFunction) {
                throw new Exception("Exam Question ID " + question.getExamQuestionId() +
                        " does not have any function containing 'success' or 'successfully'.");
            }
        }

        // Continue calculating and distributing points
        for (Map.Entry<Exam_Question, List<Postman_For_Grading>> entry : groupedByQuestion.entrySet()) {
            Exam_Question question = entry.getKey();
            List<Postman_For_Grading> functions = entry.getValue();

            // List of functions containing "success" or "successfully"
            List<Postman_For_Grading> successFunctions = functions.stream()
                    .filter(f -> {
                        String functionName = f.getPostmanFunctionName().toLowerCase();
                        String[] words = functionName.split("\\s+");
                        return Arrays.stream(words).anyMatch(word -> word.equals("success")
                                || word.equals("successfully") || word.equals("successful"));
                    })
                    .collect(Collectors.toList());

            // Total score of the question
            float totalScore = question.getExamQuestionScore();

            if (successFunctions.size() == functions.size()) {
                // In case all functions contain "success"
                float scorePerFunction = totalScore / successFunctions.size();

                successFunctions.forEach(f -> {
                    f.setScoreOfFunction(scorePerFunction);
                    f.setScorePercentage((scorePerFunction / totalExamPaperScore) * 100);
                });
            } else {
                // Normal case: dividing points between successFunctions and remainingFunctions
                List<Postman_For_Grading> remainingFunctions = functions.stream()
                        .filter(f -> !successFunctions.contains(f))
                        .collect(Collectors.toList());

                // Calculate 50% score for successFunctions
                float successScore = totalScore * 0.5f;
                float scorePerSuccessFunction = successFunctions.isEmpty() ? 0 : successScore / successFunctions.size();

                // Calculate the remaining 50% score for remainingFunctions
                float remainingScore = totalScore - successScore;
                float scorePerRemainingFunction = remainingFunctions.isEmpty() ? 0
                        : remainingScore / remainingFunctions.size();

                successFunctions.forEach(f -> {
                    f.setScoreOfFunction(scorePerSuccessFunction);
                    f.setScorePercentage((scorePerSuccessFunction / totalExamPaperScore) * 100);
                });

                remainingFunctions.forEach(f -> {
                    f.setScoreOfFunction(scorePerRemainingFunction);
                    f.setScorePercentage((scorePerRemainingFunction / totalExamPaperScore) * 100);
                });

                // Total points allocated
                float totalDistributedScore = (successFunctions.size() * scorePerSuccessFunction)
                        + (remainingFunctions.size() * scorePerRemainingFunction);

                // Handle errors if any
                float adjustment = totalScore - totalDistributedScore;
                if (!functions.isEmpty()) {
                    Postman_For_Grading firstFunction = functions.get(0);
                    float adjustedScore = firstFunction.getScoreOfFunction() + adjustment;
                    firstFunction.setScoreOfFunction(adjustedScore);
                    firstFunction.setScorePercentage((adjustedScore / totalExamPaperScore) * 100);
                }
            }
            postmanForGradingRepository.saveAll(functions);
            examPaper.setIsComfirmFile(false);
            examPaperRepository.save(examPaper);
        }
        saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                + "] [Calculate score for all postman function successfully] at [" + time + "]");

    }

    @Override
    public String updateAllPostmanCollections(Long examPaperId) {
        checkGradingProcessStatus(examPaperId);
        List<Postman_For_Grading> postmanList = postmanForGradingRepository
                .findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriorityAsc(examPaperId);

        if (postmanList.isEmpty()) {
            throw new NoSuchElementException("No Postman collections found for the given examPaperId.");
        }

        for (Postman_For_Grading postman : postmanList) {
            byte[] updatedFileCollection = processPostmanCollection(postman.getFileCollectionPostman());
            postman.setFileCollectionPostman(updatedFileCollection);

            postmanForGradingRepository.save(postman);
        }
        mergeAllFile(examPaperId);

        return "All Postman collections updated successfully.";
    }

    // Function to handle fileCollectionPostman
    private byte[] processPostmanCollection(byte[] fileCollectionPostman) {
        try {
            String jsonContent = new String(fileCollectionPostman, StandardCharsets.UTF_8);
            JSONObject collection = new JSONObject(jsonContent);
            JSONArray items = collection.getJSONArray("item");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject request = item.getJSONObject("request");

                // Delete header with key "Authorization"
                JSONArray headers = request.getJSONArray("header");
                JSONArray updatedHeaders = new JSONArray();
                for (int j = 0; j < headers.length(); j++) {
                    JSONObject header = headers.getJSONObject(j);
                    if (!"Authorization".equalsIgnoreCase(header.getString("key"))) {
                        updatedHeaders.put(header);
                    }
                }
                request.put("header", updatedHeaders);

                // Extract token value from "Authorization" header and replace it with empty
                // value for dynamic replacement
                String token = extractTokenFromBearer(headers);

                // Add request.auth with dynamic token
                JSONObject auth = new JSONObject();
                auth.put("type", "bearer");
                JSONArray bearer = new JSONArray();
                bearer.put(new JSONObject()
                        .put("key", "token")
                        .put("value", token)
                        .put("type", "string"));
                auth.put("bearer", bearer);
                request.put("auth", auth);
            }
            return collection.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing Postman collection.");
        }
    }

    // Function to extract token after "Bearer " in the Authorization header
    private String extractTokenFromBearer(JSONArray headers) {
        for (int i = 0; i < headers.length(); i++) {
            JSONObject header = headers.getJSONObject(i);
            if ("Authorization".equalsIgnoreCase(header.getString("key"))) {
                String authorizationValue = header.getString("value");
                if (authorizationValue != null && authorizationValue.startsWith("Bearer ")) {
                    return authorizationValue.substring(7).trim();
                }
            }
        }
        return null;
    }

    // Function to merge after
    public void mergeAllFile(Long examPaperId) {

        try {
            List<Postman_For_Grading> postmanList = postmanForGradingRepository
                    .findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriorityAsc(examPaperId);

            if (postmanList.isEmpty()) {
                throw new NoSuchElementException("No Postman Collection files found for Exam Paper ID: " + examPaperId);
            }

            JSONObject mergedCollection = new JSONObject();
            JSONArray mergedItems = new JSONArray();

            Exam_Paper examPaper = examPaperRepository.findById(examPaperId).orElseThrow(
                    () -> new RuntimeException("Exam Paper not existed ID: " + examPaperId));

            String examCode = (examPaper.getExam() != null) ? examPaper.getExam().getExamCode() : "Unknown";

            JSONObject info = new JSONObject();
            info.put("_postman_id", java.util.UUID.randomUUID().toString());
            info.put("name", examCode + "-" + examPaper.getExamPaperCode() + "-postman-collection");
            info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
            info.put("_exporter_id", "29781870");

            mergedCollection.put("info", info);
            for (Postman_For_Grading postman : postmanList) {
                String fileContent = new String(postman.getFileCollectionPostman(), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(fileContent);

                if (jsonObject.has("item")) {
                    JSONArray items = jsonObject.getJSONArray("item");
                    for (int i = 0; i < items.length(); i++) {
                        mergedItems.put(items.getJSONObject(i));
                    }
                }
            }
            mergedCollection.put("item", mergedItems);
            byte[] mergedFileContent = mergedCollection.toString().getBytes(StandardCharsets.UTF_8);
            examPaper.setFileCollectionPostman(mergedFileContent);
            examPaperRepository.save(examPaper);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("An error occurred when merging the Postman Collection file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An unknown error occurred: " + e.getMessage());
        }
    }

}
