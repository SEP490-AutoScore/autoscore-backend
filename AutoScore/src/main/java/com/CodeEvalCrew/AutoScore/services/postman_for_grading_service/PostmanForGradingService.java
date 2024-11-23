package com.CodeEvalCrew.AutoScore.services.postman_for_grading_service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.persistence.exceptions.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Api_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Selected_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.Content;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;
import com.CodeEvalCrew.AutoScore.repositories.account_selected_key_repository.AccountSelectedKeyRepository;
import com.CodeEvalCrew.AutoScore.repositories.content_repository.ContentRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.gherkin_scenario_repository.GherkinScenarioRepository;
import com.CodeEvalCrew.AutoScore.repositories.postman_for_grading.PostmanForGradingRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private RestTemplate restTemplate;

    private Long totalPmTest;

    // Kiểm tra các điều kiện cho phần tử đầu tiên
    private boolean isFirstElementValid(PostmanForGradingUpdateDTO firstElement) {

        return firstElement != null
                && firstElement.getPostmanForGradingId() == 0
                && "Hidden".equals(firstElement.getPostmanFunctionName())
                && firstElement.getScoreOfFunction() == 0
                && firstElement.getPostmanForGradingParentId() == 0;
    }

    @Override
    public String updatePostmanForGrading(Long examPaperId, List<PostmanForGradingUpdateDTO> updateDTOs) {
        if (updateDTOs == null || updateDTOs.isEmpty()) {
            throw new IllegalArgumentException("Danh sách updateDTOs không được để trống!");
        }

        // Kiểm tra danh sách có phần tử đầu tiên đúng yêu cầu
        if (updateDTOs.isEmpty() || !isFirstElementValid(updateDTOs.get(0))) {
            return "Phần tử đầu tiên của updateDTOs không hợp lệ.";

        }

        // Lấy danh sách các Postman_For_Grading
        List<Postman_For_Grading> postmanList = postmanForGradingRepository
                .findByExamPaper_ExamPaperIdAndStatusTrue(examPaperId);

        if (postmanList.isEmpty()) {
            throw new RuntimeException(
                    "Không tìm thấy Postman_For_Grading nào có status = true với Exam Paper ID: " + examPaperId);
        }

        // Tập hợp các ID từ danh sách postmanList
        Set<Long> validIds = postmanList.stream()
                .map(Postman_For_Grading::getPostmanForGradingId)
                .collect(Collectors.toSet());

        // Kiểm tra xem tất cả các postmanForGradingId
        for (PostmanForGradingUpdateDTO dto : updateDTOs) {
            if (dto.getPostmanForGradingId() != 0 && !validIds.contains(dto.getPostmanForGradingId())) {
                throw new RuntimeException(
                        "Postman_For_Grading ID " + dto.getPostmanForGradingId() + " không tồn tại hoặc không hợp lệ.");
            }
        }

        // Phần tử đầu tiên luôn là đối tượng đặc biệt. bỏ qua
        int orderBy = 1; // Giá trị orderBy bắt đầu từ 1
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
            } else {
                postman.setPostmanForGradingParentId(dto.getPostmanForGradingParentId());
            }

            postman.setOrderBy((long) orderBy); // Tự động set orderBy

            // Tăng giá trị orderBy cho lần tiếp theo
            orderBy++;

            // Lưu lại đối tượng đã cập nhật
            postmanForGradingRepository.save(postman);
        }

        return "Successfully";
    }

    @Override
    public String mergePostmanCollections(Long examPaperId) {
        try {
            // Lấy danh sách các Postman_For_Grading theo examPaperId
            List<Postman_For_Grading> postmanList = postmanForGradingRepository
                    .findByExamPaper_ExamPaperId(examPaperId);

            if (postmanList.isEmpty()) {
                return "Không tìm thấy file Postman Collection nào cho Exam Paper ID: " + examPaperId;
            }

            // Khởi tạo JSONObject để lưu file collection đã gộp
            JSONObject mergedCollection = new JSONObject();
            JSONArray mergedItems = new JSONArray();

            // Lấy info và item từ file đầu tiên
            Postman_For_Grading firstPostman = postmanList.get(0);
            JSONObject firstFileCollection = new JSONObject(
                    new String(firstPostman.getFileCollectionPostman(), StandardCharsets.UTF_8));

            // Lấy info và item từ file đầu tiên
            if (firstFileCollection.has("info")) {
                mergedCollection.put("info", firstFileCollection.getJSONObject("info"));
            }

            if (firstFileCollection.has("item")) {
                JSONArray firstItems = firstFileCollection.getJSONArray("item");
                for (int i = 0; i < firstItems.length(); i++) {
                    mergedItems.put(firstItems.getJSONObject(i));
                }
            }

            // Gộp các item từ các file tiếp theo
            for (int index = 1; index < postmanList.size(); index++) {
                Postman_For_Grading postman = postmanList.get(index);
                byte[] fileBytes = postman.getFileCollectionPostman();
                String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(fileContent);

                // Lấy các item và thêm vào mergedItems
                if (jsonObject.has("item")) {
                    JSONArray items = jsonObject.getJSONArray("item");
                    for (int i = 0; i < items.length(); i++) {
                        mergedItems.put(items.getJSONObject(i));
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

            return "Gộp file Postman Collection thành công cho Exam Paper ID: " + examPaperId;

        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return "Lỗi xảy ra khi gộp file Postman Collection: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi không xác định xảy ra: " + e.getMessage();
        }
    }

@Transactional
public String generatePostmanCollection(Long gherkinScenarioId) {
    // Tìm Gherkin_Scenario từ ID
    Gherkin_Scenario gherkinScenario = gherkinScenarioRepository.findById(gherkinScenarioId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Gherkin Scenario với ID: " + gherkinScenarioId));

    // Lấy Exam_Database liên kết với Exam_Paper từ Gherkin_Scenario
    Exam_Database examDatabase = examDatabaseRepository
            .findByExamPaper_ExamPaperId(gherkinScenario.getExamQuestion().getExamPaper().getExamPaperId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Exam Database liên kết với Exam Paper"));

    // Lấy authenticated user ID
    Long authenticatedUserId = Util.getAuthenticatedAccountId();

    // Lấy Account_Selected_Key cho người dùng
    Account_Selected_Key accountSelectedKey = accountSelectedKeyRepository.findByAccount_AccountId(authenticatedUserId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Account_Selected_Key cho người dùng hiện tại"));

    // Lấy AI_Api_Key từ Account_Selected_Key
    AI_Api_Key selectedAiApiKey = accountSelectedKey.getAiApiKey();
    if (selectedAiApiKey == null) {
        throw new RuntimeException("AI_Api_Key không tồn tại trong Account_Selected_Key");
    }

    // Lấy danh sách Content được sắp xếp theo orderPriority
    List<Content> orderedContents = contentRepository
            .findByPurposeOrderByOrderPriority("Generate Postman Collection");

    // Tạo prompt từ các Content và dữ liệu liên quan
    StringBuilder promptBuilder = new StringBuilder();
    orderedContents.forEach(content -> {
        if (content.getOrderPriority() == 1) {
            // Thêm dữ liệu từ Exam_Database khi orderPriority = 1
            promptBuilder.append(content.getQuestionContent())
                    .append("\nDatabase Script: ")
                    .append(examDatabase.getDatabaseScript());
        } else if (content.getOrderPriority() == 2) {
            // Thêm dữ liệu từ Exam_Question
            promptBuilder.append(content.getQuestionContent())
                    .append("\n\n\n")
                    .append("\n - Question Content: ")
                    .append(gherkinScenario.getExamQuestion().getQuestionContent())
                    .append("\n - EndPoint: ")
                    .append(gherkinScenario.getExamQuestion().getEndPoint())
                    .append("\n - Description: ")
                    .append(gherkinScenario.getExamQuestion().getDescription())
                    .append("\n - Payload: ")
                    .append(gherkinScenario.getExamQuestion().getPayload())
                    .append("\n - Payload type: ")
                    .append(gherkinScenario.getExamQuestion().getPayloadType())
                    .append("\n - Http method: ")
                    .append(gherkinScenario.getExamQuestion().getHttpMethod())
                    .append("\n - Error response: ")
                    .append(gherkinScenario.getExamQuestion().getErrorResponse())
                    .append("\n - Success response: ")
                    .append(gherkinScenario.getExamQuestion().getSucessResponse());
        } else if (content.getOrderPriority() == 3) {
            // Thêm gherkinData từ Gherkin_Scenario
            promptBuilder.append(content.getQuestionContent())
                    .append("\n")
                    .append(gherkinScenario.getGherkinData());
            System.out.println("Gherkin Data: " + gherkinScenario.getGherkinData());
        }
    });

    String prompt = new String(promptBuilder.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

    // Gọi AI API để lấy JSON Postman Collection
    String responseBody = sendToAI(prompt, selectedAiApiKey.getAiApiKey());
    if (responseBody == null) {
        throw new RuntimeException("Lỗi khi gọi AI API để tạo Postman Collection");
    }

    // Trích xuất JSON từ response body
    String collectionJson = extractJsonFromResponse(responseBody);
    if (collectionJson == null || collectionJson.isEmpty()) {
        throw new IllegalArgumentException("Không tìm thấy JSON trong phần phản hồi.");
    }

    String postmanFunctionName = runNewman(collectionJson);
    if (postmanFunctionName == null) {
        throw new RuntimeException("Newman run failed or postmanFunctionName not found.");
    }

    // Lưu Postman Collection vào Postman_For_Grading
    Postman_For_Grading postmanForGrading = new Postman_For_Grading();
    postmanForGrading.setGherkinScenario(gherkinScenario);
    postmanForGrading.setExamQuestion(gherkinScenario.getExamQuestion());
    postmanForGrading.setFileCollectionPostman(collectionJson.getBytes(StandardCharsets.UTF_8));
    postmanForGrading.setExamPaper(gherkinScenario.getExamQuestion().getExamPaper());
    postmanForGrading.setPostmanFunctionName(postmanFunctionName);
    postmanForGrading.setTotalPmTest(totalPmTest);
    postmanForGrading.setStatus(true);
    postmanForGradingRepository.save(postmanForGrading);

    return "Postman Collection được tạo và lưu thành công.";
}


    // @Transactional
    // public String generatePostmanCollection(Long gherkinScenarioId) {
    //     // Tìm Gherkin_Scenario từ ID
    //     Gherkin_Scenario gherkinScenario = gherkinScenarioRepository.findById(gherkinScenarioId)
    //             .orElseThrow(
    //                     () -> new RuntimeException("Không tìm thấy Gherkin Scenario với ID: " + gherkinScenarioId));

    //     // Lấy Exam_Database liên kết với Exam_Paper từ Gherkin_Scenario
    //     Exam_Database examDatabase = examDatabaseRepository
    //             .findByExamPaper_ExamPaperId(gherkinScenario.getExamQuestion().getExamPaper().getExamPaperId())
    //             .orElseThrow(() -> new RuntimeException("Không tìm thấy Exam Database liên kết với Exam Paper"));

    //     // Lấy AI_Info với purpose "Generate Postman Collection"
    //     List<AI_Info> aiInfos = aiInfoRepository.findByPurpose("Generate Postman Collection");
    //     if (aiInfos.isEmpty()) {
    //         throw new RuntimeException("Không tìm thấy AI_Info với mục đích 'Generate Postman Collection'");
    //     }

    //     AI_Info aiInfo = aiInfos.get(0);
    //     List<Content> orderedContents = aiInfo.getContents()
    //             .stream()
    //             .sorted((c1, c2) -> Long.compare(c1.getOrderPriority(), c2.getOrderPriority()))
    //             .collect(Collectors.toList());

    //     // Tạo prompt từ các Content và gherkinData
    //     StringBuilder promptBuilder = new StringBuilder();
    //     orderedContents.forEach(content -> {
    //         if (content.getOrderPriority() == 1) {
    //             // Thêm dữ liệu từ Exam_Database khi orderPriority = 1
    //             promptBuilder.append(content.getQuestionContent())
    //                     .append("\nDatabase Script: ")
    //                     .append(examDatabase.getDatabaseScript());
    //         } else if (content.getOrderPriority() == 2) {
    //             // Thêm dữ liệu từ Exam_Question
    //             promptBuilder.append(content.getQuestionContent())
    //                     .append("\n\n\n")
    //                     .append("\n - Question Content: ")
    //                     .append(gherkinScenario.getExamQuestion().getQuestionContent())
    //                     .append("\n - EndPoint: ")
    //                     .append(gherkinScenario.getExamQuestion().getEndPoint())
    //                     .append("\n - Description: ")
    //                     .append(gherkinScenario.getExamQuestion().getDescription())
    //                     .append("\n - Payload: ")
    //                     .append(gherkinScenario.getExamQuestion().getPayload())
    //                     .append("\n - Payload type: ")
    //                     .append(gherkinScenario.getExamQuestion().getPayloadType())
    //                     .append("\n - Http method: ")
    //                     .append(gherkinScenario.getExamQuestion().getHttpMethod())
    //                     .append("\n - Error response: ")
    //                     .append(gherkinScenario.getExamQuestion().getErrorResponse())
    //                     .append("\n - Success response: ")
    //                     .append(gherkinScenario.getExamQuestion().getSucessResponse());
    //         } else if (content.getOrderPriority() == 3) {
    //             // Thêm gherkinData từ Gherkin_Scenario
    //             promptBuilder.append(content.getQuestionContent())
    //                     .append("\n")
    //                     .append(gherkinScenario.getGherkinData());
    //             System.out.println("Gherkin Data: " + gherkinScenario.getGherkinData());

    //         }

    //     });

    //     String prompt = new String(promptBuilder.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

    //     // Gọi AI API để lấy JSON Postman Collection
    //     String responseBody = sendToAI(prompt, aiInfo.getAiApiKey());
    //     if (responseBody == null) {
    //         throw new RuntimeException("Lỗi khi gọi AI API để tạo Postman Collection");
    //     }

    //     // Trích xuất JSON từ response body
    //     String collectionJson = extractJsonFromResponse(responseBody);
    //     // Kiểm tra xem JSON có được trích xuất thành công không
    //     if (collectionJson == null || collectionJson.isEmpty()) {
    //         throw new IllegalArgumentException("Không tìm thấy JSON trong phần phản hồi.");
    //     }

    //     String postmanFunctionName = runNewman(collectionJson);

    //     if (postmanFunctionName == null) {
    //         throw new RuntimeException("Newman run failed or postmanFunctionName not found.");
    //     }

    //     // Lưu Postman Collection vào Postman_For_Grading
    //     Postman_For_Grading postmanForGrading = new Postman_For_Grading();
    //     postmanForGrading.setGherkinScenario(gherkinScenario);
    //     postmanForGrading.setExamQuestion(gherkinScenario.getExamQuestion());
    //     postmanForGrading.setFileCollectionPostman(collectionJson.getBytes(StandardCharsets.UTF_8));
    //     postmanForGrading.setExamPaper(gherkinScenario.getExamQuestion().getExamPaper());
    //     postmanForGrading.setPostmanFunctionName(postmanFunctionName);
    //     postmanForGrading.setTotalPmTest(totalPmTest);
    //     postmanForGrading.setStatus(true);
    //     postmanForGradingRepository.save(postmanForGrading);
    //     return "Postman Collection được tạo và lưu thành công.";
    // }

    // Hàm trích xuất JSON từ response body
    private String extractJsonFromResponse(String responseBody) {
        try {
            // Chuyển đổi responseBody thành JSONObject
            JSONObject jsonResponse = new JSONObject(responseBody);
            // Lấy phần JSON cần thiết
            String jsonString = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            // Bỏ dấu ```json và ``` ở đầu và cuối chuỗi
            jsonString = jsonString.replace("```json\n", "").replace("```", "").trim();

            return jsonString; // Trả về JSON đã trích xuất
        } catch (JSONException e) {
            e.printStackTrace();
            return null; // Trả về null nếu có lỗi
        }
    }

    private String runNewman(String collectionJson) {
        String postmanFunctionName = null;
        totalPmTest = 0L; // Khởi tạo biến đếm số lượng test case

        try {
            // Ghi collection JSON vào file tạm thời
            Path tempFile = Files.createTempFile("collection", ".json");
            Files.write(tempFile, collectionJson.getBytes(StandardCharsets.UTF_8));

            String newmanPath = "C:\\Users\\Admin\\AppData\\Roaming\\npm\\newman.cmd"; // Đường dẫn tới Newman
            String timeout = "1000"; // Đặt thời gian chờ

            // Tạo ProcessBuilder để chạy Newman
            ProcessBuilder processBuilder = new ProcessBuilder(
                    newmanPath,
                    "run",
                    tempFile.toAbsolutePath().toString(),
                    "--timeout", timeout);
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
        }
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
        List<Postman_For_Grading> postmanForGradingEntries = postmanForGradingRepository
                .findByExamPaper_ExamPaperId(examPaperId);

        // Chuyển đổi danh sách các thực thể thành DTO
        List<PostmanForGradingDTO> dtoList = postmanForGradingEntries.stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getStatus())) // Lọc theo status = true
                .map(entry -> {
                    PostmanForGradingDTO dto = new PostmanForGradingDTO();
                    dto.setPostmanForGradingId(entry.getPostmanForGradingId());
                    dto.setPostmanFunctionName(entry.getPostmanFunctionName());
                    dto.setScoreOfFunction(entry.getScoreOfFunction());
                    dto.setTotalPmTest(entry.getTotalPmTest());
                    dto.setOrderBy(entry.getOrderBy());
                    dto.setStatus(entry.getStatus());
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

        // Tạo phần tử mới cần thêm
        PostmanForGradingDTO newElement = new PostmanForGradingDTO();
        newElement.setPostmanForGradingId(0L);
        newElement.setPostmanFunctionName("Hidden");
        newElement.setScoreOfFunction(0F);
        newElement.setTotalPmTest(null);
        newElement.setOrderBy(null);
        newElement.setStatus(true);
        newElement.setPostmanForGradingParentId(0L);
        newElement.setExamQuestionId(null);
        newElement.setGherkinScenarioId(null);

        // Thêm phần tử mới vào đầu danh sách
        List<PostmanForGradingDTO> updatedList = new ArrayList<>();
        updatedList.add(newElement);
        updatedList.addAll(dtoList);

        // Lấy danh sách ID từ updatedList
        Set<Long> validIds = updatedList.stream()
                .map(PostmanForGradingDTO::getPostmanForGradingId)
                .collect(Collectors.toSet());

        // Cập nhật postmanForGradingParentId thành 0 nếu không tồn tại trong danh sách
        // ID
        updatedList.forEach(item -> {
            if (item.getPostmanForGradingParentId() != null
                    && !validIds.contains(item.getPostmanForGradingParentId())) {
                item.setPostmanForGradingParentId(0L);
            }
        });

        return updatedList;
    }

    // @Override
    // public List<PostmanForGradingDTO> getPostmanForGradingByExamPaperId(Long
    // examPaperId) {
    // List<Postman_For_Grading> postmanForGradingEntries =
    // postmanForGradingRepository
    // .findByExamPaper_ExamPaperId(examPaperId);

    // // Chuyển đổi danh sách các thực thể thành DTO
    // List<PostmanForGradingDTO> dtoList = postmanForGradingEntries.stream()
    // .filter(entry -> Boolean.TRUE.equals(entry.getStatus())) // Lọc theo status =
    // true
    // .map(entry -> {
    // PostmanForGradingDTO dto = new PostmanForGradingDTO();
    // dto.setPostmanForGradingId(entry.getPostmanForGradingId());
    // dto.setPostmanFunctionName(entry.getPostmanFunctionName());
    // dto.setScoreOfFunction(entry.getScoreOfFunction());
    // dto.setTotalPmTest(entry.getTotalPmTest());
    // dto.setOrderBy(entry.getOrderBy());
    // dto.setStatus(entry.getStatus());

    // // Nếu parentId là null, set giá trị thành 0
    // dto.setPostmanForGradingParentId(
    // entry.getPostmanForGradingParentId() != null
    // ? entry.getPostmanForGradingParentId()
    // : 0L
    // );

    // dto.setExamQuestionId(entry.getExamQuestion() != null
    // ? entry.getExamQuestion().getExamQuestionId()
    // : null);
    // dto.setGherkinScenarioId(entry.getGherkinScenario() != null
    // ? entry.getGherkinScenario().getGherkinScenarioId()
    // : null);
    // return dto;
    // })
    // .collect(Collectors.toList());

    // // Tạo phần tử mới cần thêm
    // PostmanForGradingDTO newElement = new PostmanForGradingDTO();
    // newElement.setPostmanForGradingId(0L);
    // newElement.setPostmanFunctionName("Hidden");
    // newElement.setScoreOfFunction(null);
    // newElement.setTotalPmTest(null);
    // newElement.setOrderBy(null);
    // newElement.setStatus(true);
    // newElement.setPostmanForGradingParentId(null);
    // newElement.setExamQuestionId(null);
    // newElement.setGherkinScenarioId(null);

    // // Thêm phần tử mới vào đầu danh sách
    // List<PostmanForGradingDTO> updatedList = new ArrayList<>();
    // updatedList.add(newElement);
    // updatedList.addAll(dtoList);

    // return updatedList;
    // }

    @Transactional
    public void updatePostmanForGradingList(List<PostmanForGradingDTO> postmanForGradingDTOs) {
        for (PostmanForGradingDTO dto : postmanForGradingDTOs) {
            // Tìm kiếm thực thể theo ID
            Postman_For_Grading postmanForGrading = postmanForGradingRepository.findById(dto.getPostmanForGradingId())
                    .orElseThrow(() -> new RuntimeException(
                            "Postman_For_Grading not found with id: " + dto.getPostmanForGradingId()));

            // Cập nhật các trường từ DTO
            postmanForGrading.setScoreOfFunction(dto.getScoreOfFunction());
            postmanForGrading.setOrderBy(dto.getOrderBy());
            postmanForGrading.setPostmanForGradingParentId(dto.getPostmanForGradingParentId());

            // Lưu lại thực thể đã cập nhật
            postmanForGradingRepository.save(postmanForGrading);
        }
    }

}
