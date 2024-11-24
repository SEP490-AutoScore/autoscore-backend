package com.CodeEvalCrew.AutoScore.services.postman_for_grading_service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingCreateDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateGetDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingGetDTO;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.transaction.Transactional;

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

    @Override
    public String generatePostmanCollection(Long gherkinScenarioId) {
        // Kiểm tra Gherkin_Scenario
        Gherkin_Scenario gherkinScenario = gherkinScenarioRepository.findById(gherkinScenarioId)
                .orElse(null);
        if (gherkinScenario == null) {
            return "Gherkin Scenario ID not found";
        }

        // Kiểm tra nếu đã có Postman_For_Grading với gherkinScenarioId và status = true
        Optional<Postman_For_Grading> existingPostman = postmanForGradingRepository
                .findByGherkinScenario_GherkinScenarioIdAndStatusTrue(gherkinScenarioId);
        if (existingPostman.isPresent()) {
            return "Active Postman collection already exists for this Gherkin Scenario ID.";
        }

        // Lấy Exam_Database
        Exam_Database examDatabase = examDatabaseRepository
                .findByExamPaper_ExamPaperId(gherkinScenario.getExamQuestion().getExamPaper().getExamPaperId())
                .orElse(null);
        if (examDatabase == null) {
            return "Exam Database not found for the associated Exam Paper";
        }

        // Lấy authenticated user ID
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        Account_Selected_Key accountSelectedKey = accountSelectedKeyRepository
                .findByAccount_AccountId(authenticatedUserId)
                .orElse(null);
        if (accountSelectedKey == null) {
            return "User has not select AI Key";
        }

        AI_Api_Key selectedAiApiKey = accountSelectedKey.getAiApiKey();
        if (selectedAiApiKey == null) {
            return "AI_Api_Key does not exist in Account_Selected_Key";
        }

        // Lấy danh sách Content sắp xếp theo orderPriority
        List<Content> orderedContents = contentRepository
                .findByPurposeOrderByOrderPriority(Purpose_Enum.GENERATE_POSTMAN_COLLECTION);

        // Tạo response tổng hợp
        StringBuilder fullResponseBuilder = new StringBuilder();

        // Gửi từng câu hỏi tới AI dựa trên orderPriority
        for (Content content : orderedContents) {
            String question = content.getQuestionAskAiContent();

            if (content.getOrderPriority() == 1) {
                question += "\nDatabase Script: " + examDatabase.getDatabaseScript();
            } else if (content.getOrderPriority() == 2) {
                question += "\n" + gherkinScenario.getGherkinData()
                       + "\n\n" +
                        "\n - Question Content: " + gherkinScenario.getExamQuestion().getQuestionContent() +
                        "\n - EndPoint: " + gherkinScenario.getExamQuestion().getEndPoint() +
                        "\n - Description: " + gherkinScenario.getExamQuestion().getDescription() +
                        "\n - Payload: " + gherkinScenario.getExamQuestion().getPayload() +
                        "\n - Payload type: " + gherkinScenario.getExamQuestion().getPayloadType() +
                        "\n - Http method: " + gherkinScenario.getExamQuestion().getHttpMethod() +
                        "\n - Error response: " + gherkinScenario.getExamQuestion().getErrorResponse() +
                        "\n - Success response: " + gherkinScenario.getExamQuestion().getSucessResponse();
            } 

            // Gửi từng câu hỏi độc lập tới AI
            String promptInUTF8 = new String(question.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            System.out.println("qrfwefraweft" + promptInUTF8);
            String response = sendToAI(promptInUTF8, selectedAiApiKey.getAiApiKey());

            if (response == null || response.isEmpty()) {
                return "Error: Failed to call AI API for orderPriority " + content.getOrderPriority();
            }

            // Xử lý câu trả lời và thêm vào response tổng hợp
            fullResponseBuilder.append(response).append("\n");

            // Khi xử lý câu hỏi cuối, lấy JSON từ phản hồi
            if (content.getOrderPriority() == 2) {
                String collectionJson = extractJsonFromResponse(response);
                if (collectionJson == null || collectionJson.isEmpty()) {
                    return "Error: JSON not found in the AI response for orderPriority 3.";
                }
            
                String postmanFunctionName = runNewman(collectionJson);
                         if (postmanFunctionName == null) {
                    return "Error: Newman execution failed or postmanFunctionName not found.";
                }

                // Lưu kết quả vào Postman_For_Grading
                Postman_For_Grading postmanForGrading = new Postman_For_Grading();
                postmanForGrading.setGherkinScenario(gherkinScenario);
                postmanForGrading.setExamQuestion(gherkinScenario.getExamQuestion());
                postmanForGrading.setFileCollectionPostman(collectionJson.getBytes(StandardCharsets.UTF_8));
                postmanForGrading.setExamPaper(gherkinScenario.getExamQuestion().getExamPaper());
                postmanForGrading.setPostmanFunctionName(postmanFunctionName);
                postmanForGrading.setTotalPmTest(totalPmTest);
                postmanForGrading.setStatus(true);
                postmanForGradingRepository.save(postmanForGrading);
                return "Postman Collection generated successfully!";
            }
        } 

        return "Unknown error!";
    }

    @Override
    public String generatePostmanCollectionMore(Long gherkinScenarioId) {
        // Kiểm tra Gherkin_Scenario
        Gherkin_Scenario gherkinScenario = gherkinScenarioRepository.findById(gherkinScenarioId)
                .orElse(null);
        if (gherkinScenario == null) {
            return "Gherkin Scenario ID not found";
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
            return "Exam Database not found for the associated Exam Paper";
        }

        // Lấy authenticated user ID
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        Account_Selected_Key accountSelectedKey = accountSelectedKeyRepository
                .findByAccount_AccountId(authenticatedUserId)
                .orElse(null);
        if (accountSelectedKey == null) {
            return "User has not select AI Key";
        }

        AI_Api_Key selectedAiApiKey = accountSelectedKey.getAiApiKey();
        if (selectedAiApiKey == null) {
            return "AI_Api_Key does not exist in Account_Selected_Key";
        }

        // Lấy danh sách Content sắp xếp theo orderPriority
        List<Content> orderedContents = contentRepository
                .findByPurposeOrderByOrderPriority(Purpose_Enum.GENERATE_POSTMAN_COLLECTION_MORE);

        // Tạo response tổng hợp
        StringBuilder fullResponseBuilder = new StringBuilder();

        // Gửi từng câu hỏi tới AI dựa trên orderPriority
        for (Content content : orderedContents) {
            String question = content.getQuestionAskAiContent();

            if (content.getOrderPriority() == 1) {
                question += "\nDatabase Script: " + examDatabase.getDatabaseScript();
            } else if (content.getOrderPriority() == 2) {
                question += "\n\n\n" +
                        "\n - Question Content: " + gherkinScenario.getExamQuestion().getQuestionContent() +
                        "\n - EndPoint: " + gherkinScenario.getExamQuestion().getEndPoint() +
                        "\n - Description: " + gherkinScenario.getExamQuestion().getDescription() +
                        "\n - Payload: " + gherkinScenario.getExamQuestion().getPayload() +
                        "\n - Payload type: " + gherkinScenario.getExamQuestion().getPayloadType() +
                        "\n - Http method: " + gherkinScenario.getExamQuestion().getHttpMethod() +
                        "\n - Error response: " + gherkinScenario.getExamQuestion().getErrorResponse() +
                        "\n - Success response: " + gherkinScenario.getExamQuestion().getSucessResponse()
                        + "\n " + fileCollectionPostmanText;
               
            }
            

            // Gửi từng câu hỏi độc lập tới AI
            String promptInUTF8 = new String(question.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            String response = sendToAI(promptInUTF8, selectedAiApiKey.getAiApiKey());

            if (response == null || response.isEmpty()) {
                return "Error: Failed to call AI API for orderPriority " + content.getOrderPriority();
            }

            // Xử lý câu trả lời và thêm vào response tổng hợp
            fullResponseBuilder.append(response).append("\n");

            // Khi xử lý câu hỏi cuối, lấy JSON từ phản hồi
            if (content.getOrderPriority() == 2) {
                String collectionJson = extractJsonFromResponse(response);
                           
        
                if (collectionJson == null || collectionJson.isEmpty()) {
                    return "Error: JSON not found in the AI response for orderPriority 2.";
                }
//   try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("D:\\Desktop\\result.txt")))) {
//         writer.write(collectionJson);
//         writer.newLine();  // Thêm một dòng mới sau khi ghi
//     } catch (IOException e) {
//         e.printStackTrace();
//         return "Error: Unable to write to file D:\\Desktop\\result.txt";
//     }
                String postmanFunctionName = runNewman(collectionJson);

                if (postmanFunctionName == null) {
                    return "Error: Newman execution failed or postmanFunctionName not found.";
                }

                // Update the existing Postman_For_Grading entity
                postmanForGrading.setFileCollectionPostman(collectionJson.getBytes(StandardCharsets.UTF_8));
                postmanForGrading.setPostmanFunctionName(postmanFunctionName);
                postmanForGrading.setTotalPmTest(totalPmTest);
                postmanForGrading.setStatus(true);
                postmanForGradingRepository.save(postmanForGrading);
                return "Postman Collection update successfully!";
            }
        }

        return "Unknown error!";
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
                new JSONObject(jsonString);  // Kiểm tra nếu jsonString là JSON hợp lệ
                return jsonString;  // Nếu là JSON hợp lệ, trả về
            } catch (JSONException e) {
                // Nếu không phải là JSON hợp lệ, thông báo lỗi
                System.err.println("Invalid JSON format: " + e.getMessage());
                return null;
            }
    
        } catch (JSONException e) {
            // Thông báo lỗi nếu không tìm thấy các phần tử cần thiết trong response
            System.err.println("Error extracting JSON from response: " + e.getMessage());
            e.printStackTrace();
            return null;  // Trả về null nếu có lỗi
        }
    }
    
    

    private String runNewman(String collectionJson) {
        String postmanFunctionName = null;
        totalPmTest = 0L; // Khởi tạo biến đếm số lượng test case
        Path tempFile = null;  // Declare tempFile outside the try block to make it accessible in the finally block

        try {
            // Generate random 20-character string for temp file name
            String randomFileName = generateRandomFileName();
            tempFile = Files.createTempFile(randomFileName, ".json"); // Create temp file
            Files.write(tempFile, collectionJson.getBytes(StandardCharsets.UTF_8)); // Write JSON to file

            String newmanPath = PathUtil.NEWMAN_CMD_PATH;
            // String timeout = "1000"; // Đặt thời gian chờ

            // Tạo ProcessBuilder để chạy Newman
            ProcessBuilder processBuilder = new ProcessBuilder(
                    newmanPath,
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
        List<Postman_For_Grading> postmanForGradingEntries = postmanForGradingRepository
                .findByExamPaper_ExamPaperId(examPaperId);

        // Chuyển đổi danh sách các thực thể thành DTO
        List<PostmanForGradingDTO> dtoList = postmanForGradingEntries.stream()
                .filter(entry -> entry.isStatus()) // Lọc theo status = true
                .map(entry -> {
                    PostmanForGradingDTO dto = new PostmanForGradingDTO();
                    dto.setPostmanForGradingId(entry.getPostmanForGradingId());
                    dto.setPostmanFunctionName(entry.getPostmanFunctionName());
                    dto.setScoreOfFunction(entry.getScoreOfFunction());
                    dto.setTotalPmTest(entry.getTotalPmTest());
                    dto.setOrderBy(entry.getOrderBy());
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

     @Override
    public String deletePostmanForGrading(Long postmanForGradingId) {
        // Kiểm tra sự tồn tại của Postman_For_Grading
        Optional<Postman_For_Grading> optionalPostman = postmanForGradingRepository.findById(postmanForGradingId);
        if (optionalPostman.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Postman_For_Grading not found with ID: " + postmanForGradingId);
        }

        Postman_For_Grading postman = optionalPostman.get();

        // Cập nhật các trường cần thiết
        postman.setStatus(false);
        postman.setExamQuestion(null); // Xóa liên kết ExamQuestion
        postman.setGherkinScenario(null); // Xóa liên kết GherkinScenario
        postman.setExamPaper(null); // Xóa liên kết ExamPaper

        // Lưu thay đổi
        postmanForGradingRepository.save(postman);

        return "Postman_For_Grading with ID: " + postmanForGradingId + " has been successfully deleted.";
    }


@Override
    public PostmanForGradingGetDTO getPostmanForGradingById(Long postmanForGradingId) {
        // Lấy Postman_For_Grading từ database
        Optional<Postman_For_Grading> optionalPostman = postmanForGradingRepository.findById(postmanForGradingId);
        if (optionalPostman.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Postman_For_Grading not found with ID: " + postmanForGradingId);
        }

        Postman_For_Grading postman = optionalPostman.get();

        // Chuyển đổi entity sang DTO
        PostmanForGradingGetDTO dto = new PostmanForGradingGetDTO();
        dto.setPostmanForGradingId(postman.getPostmanForGradingId());
        dto.setPostmanFunctionName(postman.getPostmanFunctionName());
        dto.setScoreOfFunction(postman.getScoreOfFunction());
        dto.setTotalPmTest(postman.getTotalPmTest());
        dto.setStatus(postman.isStatus());
        dto.setOrderBy(postman.getOrderBy());
        dto.setPostmanForGradingParentId(postman.getPostmanForGradingParentId());

        // Chuyển đổi dữ liệu JSON trong FileCollectionPostman (nếu không null)
        if (postman.getFileCollectionPostman() != null) {
            try {
                String jsonString = new String(postman.getFileCollectionPostman(), StandardCharsets.UTF_8);
                dto.setFileCollectionPostman(jsonString);
            } catch (Exception e) {
                throw new RuntimeException("Error while parsing JSON in FileCollectionPostman", e);
            }
        }

        // Lấy ID của các thực thể liên kết
        dto.setExamQuestionId(postman.getExamQuestion() != null ? postman.getExamQuestion().getExamQuestionId() : null);
        dto.setGherkinScenarioId(postman.getGherkinScenario() != null ? postman.getGherkinScenario().getGherkinScenarioId() : null);
        dto.setExamPaperId(postman.getExamPaper() != null ? postman.getExamPaper().getExamPaperId() : null);

        return dto;
    }

    @Override
    public PostmanForGradingGetDTO updatePostmanForGrading(Long postmanForGradingId, PostmanForGradingUpdateGetDTO updateDTO) {
        // Tìm Postman_For_Grading từ database
        Optional<Postman_For_Grading> optionalPostman = postmanForGradingRepository.findById(postmanForGradingId);
        if (optionalPostman.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Postman_For_Grading not found with ID: " + postmanForGradingId);
        }

        Postman_For_Grading postman = optionalPostman.get();

        // Cập nhật các thuộc tính từ DTO
        if (updateDTO.getPostmanFunctionName() != null) {
            postman.setPostmanFunctionName(updateDTO.getPostmanFunctionName());
        }

        if (updateDTO.getScoreOfFunction() != null) {
            postman.setScoreOfFunction(updateDTO.getScoreOfFunction());
        }

        if (updateDTO.getFileCollectionPostman() != null) {
            // Chuyển đổi JSON thành mảng byte
            try {
                byte[] fileCollectionBytes = updateDTO.getFileCollectionPostman().getBytes(StandardCharsets.UTF_8);
                postman.setFileCollectionPostman(fileCollectionBytes);
            } catch (Exception e) {
                throw new RuntimeException("Error while parsing JSON for FileCollectionPostman", e);
            }
        }

        // Lưu thay đổi vào database
        postman = postmanForGradingRepository.save(postman);

        // Chuyển đổi thành DTO để trả về
        PostmanForGradingGetDTO dto = new PostmanForGradingGetDTO();
        dto.setPostmanForGradingId(postman.getPostmanForGradingId());
        dto.setPostmanFunctionName(postman.getPostmanFunctionName());
        dto.setScoreOfFunction(postman.getScoreOfFunction());
        dto.setTotalPmTest(postman.getTotalPmTest());
        dto.setStatus(postman.isStatus());
        dto.setOrderBy(postman.getOrderBy());
        dto.setPostmanForGradingParentId(postman.getPostmanForGradingParentId());

        // Chuyển đổi FileCollectionPostman thành chuỗi JSON (nếu không null)
        if (postman.getFileCollectionPostman() != null) {
            dto.setFileCollectionPostman(new String(postman.getFileCollectionPostman(), StandardCharsets.UTF_8));
        }

        // Gắn ID của các thực thể liên kết
        dto.setExamQuestionId(postman.getExamQuestion() != null ? postman.getExamQuestion().getExamQuestionId() : null);
        dto.setGherkinScenarioId(postman.getGherkinScenario() != null ? postman.getGherkinScenario().getGherkinScenarioId() : null);
        dto.setExamPaperId(postman.getExamPaper() != null ? postman.getExamPaper().getExamPaperId() : null);

        return dto;
    }
    @Override
      public PostmanForGradingGetDTO createPostmanForGrading(PostmanForGradingCreateDTO createDTO) {
        // Khởi tạo thực thể Postman_For_Grading
        Postman_For_Grading postman = new Postman_For_Grading();

        postman.setPostmanFunctionName(createDTO.getPostmanFunctionName());
        postman.setScoreOfFunction(createDTO.getScoreOfFunction());
        postman.setStatus(true); // Mặc định là true khi tạo mới

        // Chuyển đổi FileCollectionPostman từ JSON sang byte[]
        if (createDTO.getFileCollectionPostman() != null) {
            try {
                byte[] fileCollectionBytes = createDTO.getFileCollectionPostman().getBytes(StandardCharsets.UTF_8);
                postman.setFileCollectionPostman(fileCollectionBytes);
            } catch (Exception e) {
                throw new RuntimeException("Error while parsing JSON for FileCollectionPostman", e);
            }
        }

        // Gán ExamQuestion nếu có examQuestionId
        if (createDTO.getExamQuestionId() != null) {
            Optional<Exam_Question> optionalExamQuestion = examQuestionRepository.findById(createDTO.getExamQuestionId());
            if (optionalExamQuestion.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam_Question not found with ID: " + createDTO.getExamQuestionId());
            }
            postman.setExamQuestion(optionalExamQuestion.get());
        }

        // Gán GherkinScenario nếu có gherkinScenarioId
        if (createDTO.getGherkinScenarioId() != null) {
            Optional<Gherkin_Scenario> optionalGherkinScenario = gherkinScenarioRepository.findById(createDTO.getGherkinScenarioId());
            if (optionalGherkinScenario.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gherkin_Scenario not found with ID: " + createDTO.getGherkinScenarioId());
            }
            postman.setGherkinScenario(optionalGherkinScenario.get());
        }

        // Gán ExamPaper nếu có examPaperId
        if (createDTO.getExamPaperId() != null) {
            Optional<Exam_Paper> optionalExamPaper = examPaperRepository.findById(createDTO.getExamPaperId());
            if (optionalExamPaper.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam_Paper not found with ID: " + createDTO.getExamPaperId());
            }
            postman.setExamPaper(optionalExamPaper.get());
        }

        // Lưu thực thể Postman_For_Grading vào database
        postman = postmanForGradingRepository.save(postman);

        // Chuyển đổi sang DTO để trả về
        PostmanForGradingGetDTO dto = new PostmanForGradingGetDTO();
        dto.setPostmanForGradingId(postman.getPostmanForGradingId());
        dto.setPostmanFunctionName(postman.getPostmanFunctionName());
        dto.setScoreOfFunction(postman.getScoreOfFunction());
        dto.setTotalPmTest(postman.getTotalPmTest());
        dto.setStatus(postman.isStatus());
        dto.setOrderBy(postman.getOrderBy());
        dto.setPostmanForGradingParentId(postman.getPostmanForGradingParentId());

        // Chuyển đổi FileCollectionPostman thành chuỗi JSON (nếu không null)
        if (postman.getFileCollectionPostman() != null) {
            dto.setFileCollectionPostman(new String(postman.getFileCollectionPostman(), StandardCharsets.UTF_8));
        }

        // Gắn ID của các thực thể liên kết
        dto.setExamQuestionId(postman.getExamQuestion() != null ? postman.getExamQuestion().getExamQuestionId() : null);
        dto.setGherkinScenarioId(postman.getGherkinScenario() != null ? postman.getGherkinScenario().getGherkinScenarioId() : null);
        dto.setExamPaperId(postman.getExamPaper() != null ? postman.getExamPaper().getExamPaperId() : null);

        return dto;
    }
}
