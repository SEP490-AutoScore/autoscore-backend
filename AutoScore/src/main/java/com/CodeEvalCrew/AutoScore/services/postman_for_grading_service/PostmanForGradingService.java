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
import org.springframework.web.server.ResponseStatusException;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.GradingRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingGetbyIdDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Api_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Selected_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.Content;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Purpose_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;
import com.CodeEvalCrew.AutoScore.models.Entity.Log;
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;
import com.CodeEvalCrew.AutoScore.repositories.account_selected_key_repository.AccountSelectedKeyRepository;
import com.CodeEvalCrew.AutoScore.repositories.content_repository.ContentRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.gherkin_scenario_repository.GherkinScenarioRepository;
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
    private ContentRepository contentRepository;
    @Autowired
    private IExamQuestionRepository examQuestionRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LogRepository logRepository;

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

    private boolean isFirstElementValid(PostmanForGradingUpdateDTO firstElement) {

        return firstElement != null
                && firstElement.getPostmanForGradingId() == 0
                && "Hidden".equals(firstElement.getPostmanFunctionName());

    }

    @Override
    public String updatePostmanForGrading(Long examPaperId, List<PostmanForGradingUpdateDTO> updateDTOs) {

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

            postman.setPostmanFunctionName(dto.getPostmanFunctionName());
            postman.setScoreOfFunction(dto.getScoreOfFunction());
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

            Postman_For_Grading firstPostman = postmanList.get(0);
            JSONObject firstFileCollection = new JSONObject(
                    new String(firstPostman.getFileCollectionPostman(), StandardCharsets.UTF_8));

            if (firstFileCollection.has("info")) {
                mergedCollection.put("info", firstFileCollection.getJSONObject("info"));
            } else {
                return "The first JSON file does not contain 'info' information.";
            }

            if (firstFileCollection.has("item")) {
                JSONArray firstItems = firstFileCollection.getJSONArray("item");
                if (firstItems.length() > 0) {

                    mergedItems.put(firstItems.getJSONObject(0));
                }
            } else {
                return "The first JSON file does not contain the list 'item'.";
            }

            for (int index = 1; index < postmanList.size(); index++) {
                Postman_For_Grading postman = postmanList.get(index);
                String fileContent = new String(postman.getFileCollectionPostman(), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(fileContent);

                if (jsonObject.has("item")) {
                    JSONArray items = jsonObject.getJSONArray("item");
                    if (items.length() > 0) {

                        mergedItems.put(items.getJSONObject(0));
                    }
                }
            }

            mergedCollection.put("item", mergedItems);

            byte[] mergedFileContent = mergedCollection.toString().getBytes(StandardCharsets.UTF_8);

            Exam_Paper examPaper = examPaperRepository.findById(examPaperId).orElseThrow(
                    () -> new RuntimeException("Exam Paper không tồn tại với ID: " + examPaperId));
            examPaper.setFileCollectionPostman(mergedFileContent);
            examPaper.setIsComfirmFile(false);
            examPaperRepository.save(examPaper);

            saveLog(examPaper.getExamPaperId(),
                    "Account [" + authenticatedUserId + "] [Generate gherkin successfully] at [" + time + "]");
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

        Gherkin_Scenario gherkinScenario = gherkinScenarioRepository.findById(gherkinScenarioId)
                .orElse(null);
        if (gherkinScenario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Gherkin Scenario ID not found");
        }

        Exam_Question examQuestion = gherkinScenario.getExamQuestion();
        Exam_Paper examPaper = examQuestion.getExamPaper();

        Optional<Postman_For_Grading> existingPostman = postmanForGradingRepository
                .findByGherkinScenario_GherkinScenarioIdAndStatusTrue(gherkinScenarioId);
        if (existingPostman.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Active Postman collection already exists for this Gherkin Scenario ID.");
        }

        Exam_Database examDatabase = examDatabaseRepository
                .findByExamPaper_ExamPaperId(gherkinScenario.getExamQuestion().getExamPaper().getExamPaperId())
                .orElse(null);
        if (examDatabase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Exam Database not found for the associated Exam Paper");
        }

        Account_Selected_Key accountSelectedKey = accountSelectedKeyRepository
                .findByAccount_AccountId(authenticatedUserId)
                .orElse(null);
        if (accountSelectedKey == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User has not selected an AI Key");
        }

        AI_Api_Key selectedAiApiKey = accountSelectedKey.getAiApiKey();
        if (selectedAiApiKey == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("AI_Api_Key does not exist in Account_Selected_Key");
        }

        List<Content> orderedContents = contentRepository
                .findByPurposeOrderByOrderPriority(Purpose_Enum.GENERATE_POSTMAN_COLLECTION);

        StringBuilder fullResponseBuilder = new StringBuilder();

        for (Content content : orderedContents) {
            String question = content.getQuestionAskAiContent();

            if (content.getOrderPriority() == 1) {
                question += "\nDatabase Script: " + examDatabase.getDatabaseScript();
            } else if (content.getOrderPriority() == 2) {
                question += "\n" + gherkinScenario.getGherkinData()
                        + "\n\n"
                        + "\n - Question Content: " + gherkinScenario.getExamQuestion().getQuestionContent()
                        + "\n - Allowed Roles: " + gherkinScenario.getExamQuestion().getRoleAllow()
                        + "\n - Description: " + gherkinScenario.getExamQuestion().getDescription()
                        + "\n - End point: " + gherkinScenario.getExamQuestion().getEndPoint()
                        + "\n - Http method: " + gherkinScenario.getExamQuestion().getHttpMethod()
                        + "\n - Payload type: " + gherkinScenario.getExamQuestion().getPayloadType()
                        + "\n - Validation: " + gherkinScenario.getExamQuestion().getValidation()
                        + "\n - Success response: " + gherkinScenario.getExamQuestion().getSucessResponse()
                        + "\n - Error response: " + gherkinScenario.getExamQuestion().getErrorResponse()
                        + "\n - Payload: " + gherkinScenario.getExamQuestion().getPayload();
            }

            String promptInUTF8 = new String(question.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            String response = sendToAI(promptInUTF8, selectedAiApiKey.getAiApiKey());

            if (response == null || response.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: Failed to call AI API for orderPriority " + content.getOrderPriority());
            }

            fullResponseBuilder.append(response).append("\n");

            if (content.getOrderPriority() == 2) {
                String collectionJson = extractJsonFromResponse(response);
                if (collectionJson == null || collectionJson.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Error: JSON not found in the AI response for orderPriority 3.");
                }

                String postmanFunctionName = runNewman(collectionJson);
                if (postmanFunctionName == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error: Newman execution failed or postmanFunctionName not found.");
                }

                Postman_For_Grading postmanForGrading = new Postman_For_Grading();
                postmanForGrading.setGherkinScenario(gherkinScenario);
                postmanForGrading.setExamQuestion(gherkinScenario.getExamQuestion());
                postmanForGrading.setFileCollectionPostman(collectionJson.getBytes(StandardCharsets.UTF_8));
                postmanForGrading.setExamPaper(gherkinScenario.getExamQuestion().getExamPaper());
                postmanForGrading.setPostmanFunctionName(postmanFunctionName);
                postmanForGrading.setTotalPmTest(totalPmTest);
                postmanForGrading.setStatus(true);
                postmanForGradingRepository.save(postmanForGrading);

                saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                        + "] [Generate postman script successfully] at [" + time + "]");

                return ResponseEntity.status(HttpStatus.OK)
                        .body("Postman Collection generated successfully!");
            }
        }

        saveLog(examPaper.getExamPaperId(),
                "Account [" + authenticatedUserId + "] [Generate postman script failure] at [" + time + "]");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unknown error! AI may not have responded.");
    }

    @Override
    public ResponseEntity<?> generatePostmanCollectionMore(Long gherkinScenarioId) {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        Gherkin_Scenario gherkinScenario = gherkinScenarioRepository.findById(gherkinScenarioId)
                .orElse(null);
        if (gherkinScenario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Gherkin Scenario ID not found");
        }

        Postman_For_Grading postmanForGrading = postmanForGradingRepository
                .findByGherkinScenario_GherkinScenarioIdAndStatusTrue(gherkinScenarioId)
                .orElse(null);

        String fileCollectionPostmanText = "";
        if (postmanForGrading != null && postmanForGrading.getFileCollectionPostman() != null) {
            fileCollectionPostmanText = new String(postmanForGrading.getFileCollectionPostman(),
                    StandardCharsets.UTF_8);
        }

        Exam_Database examDatabase = examDatabaseRepository
                .findByExamPaper_ExamPaperId(gherkinScenario.getExamQuestion().getExamPaper().getExamPaperId())
                .orElse(null);
        if (examDatabase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Exam Database not found for the associated Exam Paper");
        }

        Exam_Paper examPaper = examDatabase.getExamPaper();

        Account_Selected_Key accountSelectedKey = accountSelectedKeyRepository
                .findByAccount_AccountId(authenticatedUserId)
                .orElse(null);
        if (accountSelectedKey == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User has not selected an AI Key");
        }

        AI_Api_Key selectedAiApiKey = accountSelectedKey.getAiApiKey();
        if (selectedAiApiKey == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI_Api_Key does not exist in Account_Selected_Key");
        }

        List<Content> orderedContents = contentRepository
                .findByPurposeOrderByOrderPriority(Purpose_Enum.GENERATE_POSTMAN_COLLECTION_MORE);

        StringBuilder fullResponseBuilder = new StringBuilder();

        for (Content content : orderedContents) {
            String question = content.getQuestionAskAiContent();

            if (content.getOrderPriority() == 1) {
                question += "\nDatabase Script: " + examDatabase.getDatabaseScript();
            } else if (content.getOrderPriority() == 2) {
                question += "\n\n\n"
                        + "\n - Question Content: " + gherkinScenario.getExamQuestion().getQuestionContent()
                        + "\n - Allowed Roles: " + gherkinScenario.getExamQuestion().getRoleAllow()
                        + "\n - Description: " + gherkinScenario.getExamQuestion().getDescription()
                        + "\n - End point: " + gherkinScenario.getExamQuestion().getEndPoint()
                        + "\n - Http method: " + gherkinScenario.getExamQuestion().getHttpMethod()
                        + "\n - Payload type: " + gherkinScenario.getExamQuestion().getPayloadType()
                        + "\n - Validation: " + gherkinScenario.getExamQuestion().getValidation()
                        + "\n - Success response: " + gherkinScenario.getExamQuestion().getSucessResponse()
                        + "\n - Error response: " + gherkinScenario.getExamQuestion().getErrorResponse()
                        + "\n - Payload: " + gherkinScenario.getExamQuestion().getPayload()

                        + "\n " + fileCollectionPostmanText;

            }

            String promptInUTF8 = new String(question.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            String response = sendToAI(promptInUTF8, selectedAiApiKey.getAiApiKey());

            if (response == null || response.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: Failed to call AI API for orderPriority " + content.getOrderPriority());
            }

            fullResponseBuilder.append(response).append("\n");

            if (content.getOrderPriority() == 2) {
                String collectionJson = extractJsonFromResponse(response);

                if (collectionJson == null || collectionJson.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Error: JSON not found in the AI response for orderPriority 2.");
                }

                String postmanFunctionName = runNewman(collectionJson);

                if (postmanFunctionName == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error: Newman execution failed or postmanFunctionName not found.");
                }

                postmanForGrading.setFileCollectionPostman(collectionJson.getBytes(StandardCharsets.UTF_8));
                postmanForGrading.setPostmanFunctionName(postmanFunctionName);
                postmanForGrading.setTotalPmTest(totalPmTest);
                postmanForGrading.setStatus(true);
                postmanForGradingRepository.save(postmanForGrading);

                saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                        + "] [Generate Postman script successfully] at [" + time + "]");
                return ResponseEntity.status(HttpStatus.OK).body("Postman Collection updated successfully!");
            }
        }

        saveLog(examPaper.getExamPaperId(),
                "Account [" + authenticatedUserId + "] [Generate Postman script failure] at [" + time + "]");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unknown error! Possibly no response from AI.");
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

        String newmanCmdPath = PathUtil.getNewmanCmdPath();

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
                System.out.println("No assertions information found, fallback counts test cases manually.");
                totalPmTest = countTestCases(outputBuilder.toString());
            }

            if (exitCode == 0 || outputBuilder.toString().contains("executed")) {
                System.out.println("Tổng số test case đã thực thi: " + totalPmTest);
                return postmanFunctionName;
            } else {
                System.err.println("Newman run failed.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
                    return dto;
                })
                .collect(Collectors.toList());

        PostmanForGradingDTO rootElement = new PostmanForGradingDTO();
        rootElement.setPostmanForGradingId(0L);
        rootElement.setPostmanFunctionName("Hidden");
        rootElement.setScoreOfFunction(0F);
        rootElement.setTotalPmTest(null);
        rootElement.setOrderPriority(0L);
        rootElement.setStatus(true);
        rootElement.setPostmanForGradingParentId(0L);
        rootElement.setExamQuestionId(null);
        rootElement.setGherkinScenarioId(null);

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
    public String deletePostmanForGrading(List<Long> postmanForGradingIds, Long examQuestionId) {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        Exam_Question examQuestion = examQuestionRepository.findById(examQuestionId)
                .orElseThrow(() -> new NoSuchElementException("Exam Question not exists"));
        Exam_Paper examPaper2 = examQuestion.getExamPaper();

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

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        Exam_Question examQuestion = examQuestionRepository.findById(examQuestionId)
                .orElseThrow(() -> new NoSuchElementException("Exam Question not exists"));
        Exam_Paper examPaper = examQuestion.getExamPaper();

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
    public ResponseEntity<?> calculateScores(List<GradingRequestDTO> requests, Long examPaperId, Long examQuestionId) {
        try {

            Long authenticatedUserId = Util.getAuthenticatedAccountId();
            LocalDateTime time = Util.getCurrentDateTime();

            Float totalScorePercentage = requests.stream()
                    .map(GradingRequestDTO::getScorePercentage)
                    .filter(Objects::nonNull)
                    .reduce(0f, Float::sum);

            if (Math.abs(totalScorePercentage - 100f) > 0.0001f) {
                throw new IllegalArgumentException(
                        "Total score percentage across all requests must equal 100. Current total: "
                                + totalScorePercentage);
            }

            Exam_Question examQuestion = examQuestionRepository.findById(examQuestionId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("Exam question not found with ID: " + examQuestionId));

            Exam_Paper examPaper = examQuestion.getExamPaper();

            Float examQuestionScore = examQuestion.getExamQuestionScore();

            for (GradingRequestDTO request : requests) {
                List<Long> postmanForGradingIds = request.getPostmanForGradingIds();
                Float scorePercentage = request.getScorePercentage();

                if (postmanForGradingIds == null || postmanForGradingIds.isEmpty()) {
                    throw new IllegalArgumentException(
                            "PostmanForGradingIds cannot be null or empty in request: " + request);
                }
                if (scorePercentage == null) {
                    throw new IllegalArgumentException("ScorePercentage cannot be null in request: " + request);
                }
                if (examQuestionScore == null) {
                    throw new IllegalStateException("ExamQuestionScore is null for ExamQuestion ID: " + examQuestionId);
                }

                Float normalizedScorePercentage = Math.round((scorePercentage / 100) * 1_000_000f) / 1_000_000f;

                Float totalScoreForGroup = Math.round((normalizedScorePercentage * examQuestionScore) * 1_000_000f)
                        / 1_000_000f;

                Float percentageForEach = Math
                        .round((normalizedScorePercentage / postmanForGradingIds.size()) * 1_000_000f) / 1_000_000f;
                Float scoreForEach = Math.round((totalScoreForGroup / postmanForGradingIds.size()) * 1_000_000f)
                        / 1_000_000f;

                Float calculatedTotal = 0f;
                int index = 0;

                for (Long postmanForGradingId : postmanForGradingIds) {
                    Postman_For_Grading postmanEntry = postmanForGradingRepository.findById(postmanForGradingId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Postman entry not found with ID: " + postmanForGradingId));

                    if (index == postmanForGradingIds.size() - 1) {

                        postmanEntry.setScoreOfFunction(totalScoreForGroup - calculatedTotal);
                        postmanEntry.setScorePercentage(normalizedScorePercentage - (percentageForEach * index));
                    } else {
                        postmanEntry.setScoreOfFunction(scoreForEach);
                        postmanEntry.setScorePercentage(percentageForEach);
                        calculatedTotal += scoreForEach;
                    }

                    postmanForGradingRepository.save(postmanEntry);
                    index++;
                }
            }

            examPaper.setIsComfirmFile(false);
            saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                    + "] [Update score of postman script successfully] at [" + time + "]");
            return ResponseEntity.ok("Scores calculated and updated successfully");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

}
