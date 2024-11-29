package com.CodeEvalCrew.AutoScore.services.postman_for_grading_service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;
import com.CodeEvalCrew.AutoScore.repositories.account_selected_key_repository.AccountSelectedKeyRepository;
import com.CodeEvalCrew.AutoScore.repositories.content_repository.ContentRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.gherkin_scenario_repository.GherkinScenarioRepository;
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

    private Long totalPmTest;

    // Kiểm tra các điều kiện cho phần tử đầu tiên
    private boolean isFirstElementValid(PostmanForGradingUpdateDTO firstElement) {

        return firstElement != null
                && firstElement.getPostmanForGradingId() == 0
                && "Hidden".equals(firstElement.getPostmanFunctionName());

    }

    @Override
    public String updatePostmanForGrading(Long examPaperId, List<PostmanForGradingUpdateDTO> updateDTOs) {
        if (updateDTOs == null || updateDTOs.isEmpty()) {
            throw new IllegalArgumentException("Danh sách updateDTOs không được để trống!");
        }

        // Kiểm tra phần tử đầu tiên đúng yêu cầu
        if (updateDTOs.isEmpty() || !isFirstElementValid(updateDTOs.get(0))) {
            return "Phần tử đầu tiên của updateDTOs không hợp lệ.";
        }

        // Phần tử đầu tiên luôn là đối tượng đặc biệt. bỏ qua
        int orderPriority = 1; // Giá trị orderBy bắt đầu từ 1
        for (int i = 1; i < updateDTOs.size(); i++) {
            PostmanForGradingUpdateDTO dto = updateDTOs.get(i);

            if (dto.getPostmanForGradingId() == 0) {
                throw new RuntimeException(
                        "Phần tử trong danh sách không hợp lệ: postmanForGradingId không được là 0 ngoài phần tử đầu tiên.");
            }

            // Tìm đối tượng Postman_For_Grading tương ứng với ID
            Postman_For_Grading postman = postmanForGradingRepository.findById(dto.getPostmanForGradingId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy Postman_For_Grading với ID: " + dto.getPostmanForGradingId()));

            // Cập nhật các thông tin
            postman.setPostmanFunctionName(dto.getPostmanFunctionName());
            postman.setScoreOfFunction(dto.getScoreOfFunction());
            postman.setPostmanForGradingParentId(dto.getPostmanForGradingParentId());

            // Kiểm tra nếu PostmanForGradingParentId = 0 thì set thành null
            if (dto.getPostmanForGradingParentId() == 0) {
                postman.setPostmanForGradingParentId(null);
            }

            postman.setOrderPriority((long) orderPriority); // Tự động set orderBy

            // Tăng giá trị orderBy cho lần tiếp theo
            orderPriority++;

            // Lưu lại đối tượng đã cập nhật
            postmanForGradingRepository.save(postman);
        }

        // Cập nhật isComfirmFile của Exam_Paper (có thể thêm điều kiện kiểm tra)
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Exam_Paper với ID: " + examPaperId));

        // Cập nhật giá trị isComfirmFile

        String jsonFile;
        try {
            jsonFile = new String(examPaper.getFileCollectionPostman(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Không thể đọc fileCollectionPostman", e);
        }

        // Parse JSON thành đối tượng JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(jsonFile);
        } catch (Exception e) {
            throw new RuntimeException("Không thể parse JSON từ fileCollectionPostman", e);
        }

        // Kiểm tra và thao tác trên trường "item"
        JsonNode itemsNode = rootNode.path("item");
        if (!itemsNode.isArray()) {
            throw new RuntimeException("Cấu trúc JSON không hợp lệ: thiếu trường 'item' hoặc không phải dạng mảng");
        }

        // Chuyển items thành danh sách để thao tác
        List<JsonNode> itemsList = new ArrayList<>();
        itemsNode.forEach(itemsList::add);

        // Sắp xếp danh sách items dựa trên thứ tự trong updateDTOs
        List<String> orderedNames = updateDTOs.stream()
                .map(PostmanForGradingUpdateDTO::getPostmanFunctionName)
                .collect(Collectors.toList());

        itemsList.sort(Comparator.comparingInt(item -> {
            String name = item.path("name").asText();
            return orderedNames.indexOf(name);
        }));

        // Gán danh sách items đã sắp xếp lại vào JSON
        ArrayNode sortedItemsNode = objectMapper.createArrayNode();
        itemsList.forEach(sortedItemsNode::add);
        ((ObjectNode) rootNode).set("item", sortedItemsNode);

        // Chuyển JSON đã cập nhật về byte[] và cập nhật lại Exam_Paper
        try {
            examPaper.setFileCollectionPostman(objectMapper.writeValueAsBytes(rootNode));
        } catch (Exception e) {
            throw new RuntimeException("Không thể cập nhật fileCollectionPostman", e);
        }

        examPaper.setIsComfirmFile(false);
        // Lưu lại đối tượng Exam_Paper đã cập nhật
        examPaperRepository.save(examPaper);

        return "Successfully";
    }

    @Override
    public String mergePostmanCollections(Long examPaperId) {
        try {
            // Lấy danh sách các Postman_For_Grading theo examPaperId
            List<Postman_For_Grading> postmanList = postmanForGradingRepository

                    .findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriorityAsc(examPaperId);

            if (postmanList.isEmpty()) {
                return "Không tìm thấy file Postman Collection nào cho Exam Paper ID: " + examPaperId;
            }

            // Khởi tạo JSONObject để lưu file collection đã gộp
            JSONObject mergedCollection = new JSONObject();
            JSONArray mergedItems = new JSONArray();

            // Lấy info và item đầu tiên từ file JSON đầu tiên
            Postman_For_Grading firstPostman = postmanList.get(0);
            JSONObject firstFileCollection = new JSONObject(
                    new String(firstPostman.getFileCollectionPostman(), StandardCharsets.UTF_8));

            if (firstFileCollection.has("info")) {
                mergedCollection.put("info", firstFileCollection.getJSONObject("info"));
            } else {
                return "File JSON đầu tiên không chứa thông tin 'info'.";
            }

            if (firstFileCollection.has("item")) {
                JSONArray firstItems = firstFileCollection.getJSONArray("item");
                if (firstItems.length() > 0) {
                    // Chỉ lấy item.name đầu tiên
                    mergedItems.put(firstItems.getJSONObject(0));
                }
            } else {
                return "File JSON đầu tiên không chứa danh sách 'item'.";
            }

            // Gộp item.name đầu tiên từ các file JSON tiếp theo
            for (int index = 1; index < postmanList.size(); index++) {
                Postman_For_Grading postman = postmanList.get(index);
                String fileContent = new String(postman.getFileCollectionPostman(), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(fileContent);

                if (jsonObject.has("item")) {
                    JSONArray items = jsonObject.getJSONArray("item");
                    if (items.length() > 0) {
                        // Chỉ lấy item.name đầu tiên
                        mergedItems.put(items.getJSONObject(0));
                    }
                }
            }

            // Gán danh sách item đã gộp vào mergedCollection
            mergedCollection.put("item", mergedItems);

            // Chuyển JSONObject mergedCollection thành byte[]
            byte[] mergedFileContent = mergedCollection.toString().getBytes(StandardCharsets.UTF_8);

            // Lưu file đã gộp vào Exam_Paper
            Exam_Paper examPaper = examPaperRepository.findById(examPaperId).orElseThrow(
                    () -> new RuntimeException("Exam Paper không tồn tại với ID: " + examPaperId));
            examPaper.setFileCollectionPostman(mergedFileContent);
            examPaper.setIsComfirmFile(false);
            examPaperRepository.save(examPaper);

            return "Successfully merged Postman Collections for examPaperId " + examPaperId;

        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return "Lỗi xảy ra khi gộp file Postman Collection: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi không xác định xảy ra: " + e.getMessage();
        }
    }

    @Override
    public ResponseEntity<?> generatePostmanCollection(Long gherkinScenarioId) {
        // Kiểm tra Gherkin_Scenario
        Gherkin_Scenario gherkinScenario = gherkinScenarioRepository.findById(gherkinScenarioId)
                .orElse(null);
        if (gherkinScenario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Gherkin Scenario ID not found");
        }

        // Kiểm tra nếu đã có Postman_For_Grading với gherkinScenarioId và status = true
        Optional<Postman_For_Grading> existingPostman = postmanForGradingRepository
                .findByGherkinScenario_GherkinScenarioIdAndStatusTrue(gherkinScenarioId);
        if (existingPostman.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Active Postman collection already exists for this Gherkin Scenario ID.");
        }

        // Lấy Exam_Database
        Exam_Database examDatabase = examDatabaseRepository
                .findByExamPaper_ExamPaperId(gherkinScenario.getExamQuestion().getExamPaper().getExamPaperId())
                .orElse(null);
        if (examDatabase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Exam Database not found for the associated Exam Paper");
        }

        // Lấy authenticated user ID
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

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

        // Lấy danh sách Content sắp xếp theo orderPriority
        List<Content> orderedContents = contentRepository
                .findByPurposeOrderByOrderPriority(Purpose_Enum.GENERATE_POSTMAN_COLLECTION);

        // Tạo response tổng hợp
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

            // Gửi từng câu hỏi độc lập tới AI
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
                return ResponseEntity.status(HttpStatus.OK)
                        .body("Postman Collection generated successfully!");
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unknown error! AI may not have responded.");
    }

    @Override
    public ResponseEntity<?> generatePostmanCollectionMore(Long gherkinScenarioId) {
        // Kiểm tra Gherkin_Scenario
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

        // Lấy Exam_Database
        Exam_Database examDatabase = examDatabaseRepository
                .findByExamPaper_ExamPaperId(gherkinScenario.getExamQuestion().getExamPaper().getExamPaperId())
                .orElse(null);
        if (examDatabase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Exam Database not found for the associated Exam Paper");
        }

        // Lấy authenticated user ID
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

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

        // Lấy danh sách Content sắp xếp theo orderPriority
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

                // Update the existing Postman_For_Grading entity
                postmanForGrading.setFileCollectionPostman(collectionJson.getBytes(StandardCharsets.UTF_8));
                postmanForGrading.setPostmanFunctionName(postmanFunctionName);
                postmanForGrading.setTotalPmTest(totalPmTest);
                postmanForGrading.setStatus(true);
                postmanForGradingRepository.save(postmanForGrading);

                return ResponseEntity.status(HttpStatus.OK).body("Postman Collection updated successfully!");
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unknown error! Possibly no response from AI.");
    }

    // Hàm trích xuất JSON từ response body
    private String extractJsonFromResponse(String responseBody) {
        try {
            // Chuyển đổi responseBody thành JSONObject
            JSONObject jsonResponse = new JSONObject(responseBody);

            // Kiểm tra sự tồn tại của các phần tử cần thiết trong response
            if (!jsonResponse.has("candidates")) {
                throw new JSONException("Missing 'candidates' field in response.");
            }

            // Lấy phần JSON cần thiết từ candidates -> content -> parts -> text
            String jsonString = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            // Bỏ dấu ```json và ``` ở đầu và cuối chuỗi (nếu có)
            jsonString = jsonString.replaceAll("(?s)^```json\\n", "").replaceAll("(?s)```$", "").trim();

            // Loại bỏ bất kỳ phần giải thích nào sau chuỗi JSON
            // Tìm phần JSON hợp lệ, bỏ qua tất cả văn bản sau nó
            int startIndex = jsonString.indexOf("{");
            int endIndex = jsonString.lastIndexOf("}");

            if (startIndex != -1 && endIndex != -1) {
                jsonString = jsonString.substring(startIndex, endIndex + 1);
            }

            // Kiểm tra nếu chuỗi có thể là JSON hợp lệ
            try {
                new JSONObject(jsonString); // Kiểm tra nếu jsonString là JSON hợp lệ
                return jsonString; // Nếu là JSON hợp lệ, trả về
            } catch (JSONException e) {
                // Nếu không phải là JSON hợp lệ, thông báo lỗi
                System.err.println("Invalid JSON format: " + e.getMessage());
                return null;
            }

        } catch (JSONException e) {
            // Thông báo lỗi nếu không tìm thấy các phần tử cần thiết trong response
            System.err.println("Error extracting JSON from response: " + e.getMessage());
            e.printStackTrace();
            return null; // Trả về null nếu có lỗi
        }
    }

    private String runNewman(String collectionJson) {
        String postmanFunctionName = null;
        totalPmTest = 0L; // Khởi tạo biến đếm số lượng test case
        Path tempFile = null; // Declare tempFile outside the try block to make it accessible in the finally
                              // block
        String newmanCmdPath = PathUtil.getNewmanCmdPath();

        try {
            // Generate random 20-character string for temp file name
            String randomFileName = generateRandomFileName();
            tempFile = Files.createTempFile(randomFileName, ".json"); // Create temp file
            Files.write(tempFile, collectionJson.getBytes(StandardCharsets.UTF_8)); // Write JSON to file

            // String timeout = "1000"; // Đặt thời gian chờ

            // Tạo ProcessBuilder để chạy Newman
            ProcessBuilder processBuilder = new ProcessBuilder(
                    newmanCmdPath,
                    "run",
                    tempFile.toAbsolutePath().toString());
            // "--timeout", timeout);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder outputBuilder = new StringBuilder();
            boolean assertionsFound = false; // Biến kiểm tra đã tìm thấy dòng assertions
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line).append("\n");

                // Lấy tên function từ dòng chứa dấu '→'
                if (line.contains("→")) {
                    postmanFunctionName = line.substring(line.indexOf("→") + 1).trim();
                }

                // Đếm số lượng assertions từ phần thống kê
                if (line.contains("│              assertions │")) {
                    try {
                        String[] parts = line.trim().split("│");
                        if (parts.length > 2) {
                            // Lấy số lượng assertions từ cột "executed"
                            totalPmTest = Long.parseLong(parts[2].trim());
                            assertionsFound = true; // Đã tìm thấy thông tin assertions
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Lỗi khi chuyển đổi số lượng assertions: " + e.getMessage());
                        totalPmTest = 0L; // Đặt giá trị mặc định nếu gặp lỗi
                    }
                }
            }

            int exitCode = process.waitFor();

            // Nếu không tìm thấy assertions, fallback đếm các dòng test case
            if (!assertionsFound) {
                System.out.println("Không tìm thấy thông tin assertions, fallback đếm test case thủ công.");
                totalPmTest = countTestCases(outputBuilder.toString());
            }

            // Kiểm tra exit code và output
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

    // Helper method to generate a random 20-character string
    private String generateRandomFileName() {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomName = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            int index = (int) (Math.random() * characters.length());
            randomName.append(characters.charAt(index));
        }
        return randomName.toString();
    }

    // Hàm hỗ trợ để đếm các test case theo định dạng cũ
    private long countTestCases(String output) {
        long count = 0;
        try (BufferedReader reader = new BufferedReader(new StringReader(output))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Đếm các dòng bắt đầu bằng số thứ tự (ví dụ: "1.", "2.", "3.")
                if (line.trim().matches("^\\d+\\..*")) {
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi đếm test case: " + e.getMessage());
        }
        return count;
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
    public List<PostmanForGradingDTO> getPostmanForGradingByExamPaperId(Long examPaperId) {
        // Lấy danh sách các thực thể từ repository
        List<Postman_For_Grading> postmanForGradingEntries = postmanForGradingRepository
                .findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriorityAsc(examPaperId);

        // Chuyển đổi thành danh sách DTO
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

        // Tạo node gốc (Hidden)
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

        // Thêm node gốc vào danh sách
        List<PostmanForGradingDTO> updatedList = new ArrayList<>();
        updatedList.add(rootElement);
        updatedList.addAll(dtoList);

        // Tạo một Map để truy cập nhanh các node bằng ID
        Map<Long, PostmanForGradingDTO> dtoMap = updatedList.stream()
                .collect(Collectors.toMap(PostmanForGradingDTO::getPostmanForGradingId, dto -> dto));

        // Cập nhật các node có parentId không tồn tại thành 0
        updatedList.forEach(dto -> {
            Long parentId = dto.getPostmanForGradingParentId();
            if (parentId != null && parentId != 0L && !dtoMap.containsKey(parentId)) {
                dto.setPostmanForGradingParentId(0L);
            }
        });

        // Phát hiện và xử lý chu trình
        Set<Long> visited = new HashSet<>();
        Set<Long> inRecStack = new HashSet<>();
        List<Long> cyclicIds = new ArrayList<>();

        for (PostmanForGradingDTO dto : updatedList) {
            if (!visited.contains(dto.getPostmanForGradingId())) {
                detectCycle(dto.getPostmanForGradingId(), dtoMap, visited, inRecStack, cyclicIds);
            }
        }

        // Nếu phát hiện chu trình, sửa ParentId về 0 (Hidden)
        if (!cyclicIds.isEmpty()) {
            updatedList.forEach(dto -> {
                if (cyclicIds.contains(dto.getPostmanForGradingId())) {
                    dto.setPostmanForGradingParentId(0L);
                }
            });
        }

        return updatedList;
    }

    // Hàm phát hiện chu trình sử dụng DFS
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
    public String deletePostmanForGrading(List<Long> postmanForGradingIds) {
        StringBuilder response = new StringBuilder();

        for (Long postmanForGradingId : postmanForGradingIds) {
            // Kiểm tra sự tồn tại của Postman_For_Grading
            Optional<Postman_For_Grading> optionalPostman = postmanForGradingRepository.findById(postmanForGradingId);
            if (optionalPostman.isEmpty()) {
                response.append("Postman_For_Grading not found with ID: " + postmanForGradingId + "\n");
                continue; // Skip this ID and continue with the next one
            }

            Postman_For_Grading postman = optionalPostman.get();
            // Lấy Exam_Paper từ Postman_For_Grading
            Exam_Paper examPaper = postman.getExamPaper();
            if (examPaper != null) {
                // Cập nhật isComfirmFile thành false
                examPaper.setIsComfirmFile(false);
                examPaperRepository.save(examPaper); // Lưu thay đổi vào cơ sở dữ liệu
            }

            // Cập nhật các trường cần thiết
            postman.setStatus(false);
            postman.setPostmanFunctionName(null);
            postman.setExamQuestion(null); // Xóa liên kết ExamQuestion
            postman.setGherkinScenario(null); // Xóa liên kết GherkinScenario
            // postman.setExamPaper(null); // Xóa liên kết ExamPaper

            // Lưu thay đổi
            postmanForGradingRepository.save(postman);

            response.append(
                    "Postman_For_Grading with ID: " + postmanForGradingId + " has been successfully deleted.\n");
        }

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

        // Chuyển đổi fileCollectionPostman từ byte[] sang String
        if (postmanForGrading.getFileCollectionPostman() != null) {
            String fileCollectionJson = new String(postmanForGrading.getFileCollectionPostman(),
                    StandardCharsets.UTF_8);
            dto.setFileCollectionPostman(fileCollectionJson);
        }

        return ResponseEntity.ok(dto);
    }

    @Override
    public String updateExamQuestionId(Long postmanForGradingId, Long examQuestionId) {
        // Tìm Postman_For_Grading theo ID
        Optional<Postman_For_Grading> optionalPostmanForGrading = postmanForGradingRepository
                .findById(postmanForGradingId);

        if (optionalPostmanForGrading.isEmpty()) {
            return "PostmanForGrading không tồn tại với ID: " + postmanForGradingId;
        }

        Postman_For_Grading postmanForGrading = optionalPostmanForGrading.get();

        // Tìm Exam_Question theo ID
        Optional<Exam_Question> optionalExamQuestion = examQuestionRepository.findById(examQuestionId);

        if (optionalExamQuestion.isEmpty()) {
            return "ExamQuestion không tồn tại với ID: " + examQuestionId;
        }

        Exam_Question examQuestion = optionalExamQuestion.get();

        // Cập nhật Exam_Question trong Postman_For_Grading
        postmanForGrading.setExamQuestion(examQuestion);

        // Lưu lại thay đổi
        postmanForGradingRepository.save(postmanForGrading);

        return "Cập nhật thành công PostmanForGrading với ID: " + postmanForGradingId + " và ExamQuestion ID: "
                + examQuestionId;
    }

    @Override
    public ResponseEntity<?> calculateScores(List<GradingRequestDTO> requests, Long examPaperId, Long examQuestionId) {
        try {
            // Tính tổng scorePercentage
            Float totalScorePercentage = requests.stream()
                    .map(GradingRequestDTO::getScorePercentage)
                    .filter(Objects::nonNull) // Bỏ qua các giá trị null
                    .reduce(0f, Float::sum); // Tổng hợp

            // Kiểm tra tổng có bằng 100 hay không (cho phép sai số nhỏ)
            if (Math.abs(totalScorePercentage - 100f) > 0.0001f) { // Sai số nhỏ để tránh lỗi làm tròn
                throw new IllegalArgumentException(
                        "Total score percentage across all requests must equal 100. Current total: "
                                + totalScorePercentage);
            }

            // Lấy Exam_Question tương ứng
            Exam_Question examQuestion = examQuestionRepository.findById(examQuestionId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("Exam question not found with ID: " + examQuestionId));

            Float examQuestionScore = examQuestion.getExamQuestionScore(); // Điểm tối đa của câu hỏi

            for (GradingRequestDTO request : requests) {
                List<Long> postmanForGradingIds = request.getPostmanForGradingIds();
                Float scorePercentage = request.getScorePercentage();

                // Kiểm tra dữ liệu đầu vào
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

                // Chuyển đổi scorePercentage từ phần trăm sang thập phân
                Float normalizedScorePercentage = Math.round((scorePercentage / 100) * 1_000_000f) / 1_000_000f;

                // Tổng điểm dành cho nhóm này
                Float totalScoreForGroup = Math.round((normalizedScorePercentage * examQuestionScore) * 1_000_000f)
                        / 1_000_000f;

                // Tính tỷ lệ phần trăm và điểm cho từng mục
                Float percentageForEach = Math
                        .round((normalizedScorePercentage / postmanForGradingIds.size()) * 1_000_000f) / 1_000_000f;
                Float scoreForEach = Math.round((totalScoreForGroup / postmanForGradingIds.size()) * 1_000_000f)
                        / 1_000_000f;

                Float calculatedTotal = 0f; // Tổng đã tính
                int index = 0;

                for (Long postmanForGradingId : postmanForGradingIds) {
                    Postman_For_Grading postmanEntry = postmanForGradingRepository.findById(postmanForGradingId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Postman entry not found with ID: " + postmanForGradingId));

                    if (index == postmanForGradingIds.size() - 1) {
                        // Bù sai số cho mục cuối cùng
                        postmanEntry.setScoreOfFunction(totalScoreForGroup - calculatedTotal);
                        postmanEntry.setScorePercentage(normalizedScorePercentage - (percentageForEach * index));
                    } else {
                        postmanEntry.setScoreOfFunction(scoreForEach);
                        postmanEntry.setScorePercentage(percentageForEach);
                        calculatedTotal += scoreForEach;
                    }

                    postmanForGradingRepository.save(postmanEntry); // Lưu thay đổi vào DB
                    index++;
                }
            }

            return ResponseEntity.ok("Scores calculated and updated successfully");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    // @Override
    // public ResponseEntity<?> calculateScores(List<GradingRequestDTO> requests,
    // Long examPaperId, Long examQuestionId) {
    // try {
    // // Lấy Exam_Question tương ứng
    // Exam_Question examQuestion = examQuestionRepository.findById(examQuestionId)
    // .orElseThrow(() -> new IllegalArgumentException("Exam question not found with
    // ID: " + examQuestionId));

    // Float examQuestionScore = examQuestion.getExamQuestionScore(); // Điểm tối đa
    // của câu hỏi

    // for (GradingRequestDTO request : requests) {
    // List<Long> postmanForGradingIds = request.getPostmanForGradingIds();
    // Float scorePercentage = request.getScorePercentage();

    // if (postmanForGradingIds.isEmpty() || scorePercentage == null ||
    // examQuestionScore == null) {
    // continue; // Bỏ qua nếu dữ liệu không hợp lệ
    // }

    // // Chuyển đổi scorePercentage từ phần trăm sang thập phân
    // Float normalizedScorePercentage = scorePercentage / 100;

    // // Tổng điểm dành cho nhóm này
    // Float totalScoreForGroup = normalizedScorePercentage * examQuestionScore;

    // // Tính tỷ lệ phần trăm và điểm cho từng mục
    // Float percentageForEach = normalizedScorePercentage /
    // postmanForGradingIds.size(); // Tỷ lệ phần trăm mỗi mục
    // Float scoreForEach = totalScoreForGroup / postmanForGradingIds.size();

    // Float calculatedTotal = 0f; // Tổng đã tính
    // int index = 0;

    // for (Long postmanForGradingId : postmanForGradingIds) {
    // Postman_For_Grading postmanEntry =
    // postmanForGradingRepository.findById(postmanForGradingId)
    // .orElseThrow(() -> new IllegalArgumentException("Postman entry not found with
    // ID: " + postmanForGradingId));

    // // Xử lý mục cuối cùng để bù sai số
    // if (index == postmanForGradingIds.size() - 1) {
    // postmanEntry.setScoreOfFunction(totalScoreForGroup - calculatedTotal); // Bù
    // sai số
    // postmanEntry.setScorePercentage(normalizedScorePercentage -
    // (percentageForEach * index)); // Bù sai số cho tỷ lệ
    // } else {
    // postmanEntry.setScoreOfFunction(scoreForEach);
    // postmanEntry.setScorePercentage(percentageForEach);
    // calculatedTotal += scoreForEach; // Cộng dồn tổng đã tính
    // }

    // postmanForGradingRepository.save(postmanEntry); // Lưu thay đổi vào DB
    // index++;
    // }
    // }

    // return ResponseEntity.ok("Scores calculated and updated successfully");

    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("An error occurred: " + e.getMessage());
    // }
    // }

}
