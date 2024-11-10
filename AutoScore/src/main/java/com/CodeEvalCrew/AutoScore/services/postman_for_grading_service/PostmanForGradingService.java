package com.CodeEvalCrew.AutoScore.services.postman_for_grading_service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.persistence.exceptions.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Info;
import com.CodeEvalCrew.AutoScore.models.Entity.Content;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;
import com.CodeEvalCrew.AutoScore.repositories.ai_info_repository.AIInfoRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.gherkin_scenario_repository.GherkinScenarioRepository;
import com.CodeEvalCrew.AutoScore.repositories.postman_for_grading.PostmanForGradingRepository;
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
    private AIInfoRepository aiInfoRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Transactional
    public String generatePostmanCollection(Long gherkinScenarioId) {
        // Tìm Gherkin_Scenario từ ID
        Gherkin_Scenario gherkinScenario = gherkinScenarioRepository.findById(gherkinScenarioId)
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy Gherkin Scenario với ID: " + gherkinScenarioId));

        // Lấy Exam_Database liên kết với Exam_Paper từ Gherkin_Scenario
        Exam_Database examDatabase = examDatabaseRepository
                .findByExamPaper_ExamPaperId(gherkinScenario.getExamQuestion().getExamPaper().getExamPaperId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Exam Database liên kết với Exam Paper"));

        // Lấy AI_Info với purpose "Generate Postman Collection"
        List<AI_Info> aiInfos = aiInfoRepository.findByPurpose("Generate Postman Collection");
        if (aiInfos.isEmpty()) {
            throw new RuntimeException("Không tìm thấy AI_Info với mục đích 'Generate Postman Collection'");
        }

        AI_Info aiInfo = aiInfos.get(0);
        List<Content> orderedContents = aiInfo.getContents()
                .stream()
                .sorted((c1, c2) -> Long.compare(c1.getOrderPriority(), c2.getOrderPriority()))
                .collect(Collectors.toList());

        // Tạo prompt từ các Content và gherkinData
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
        String responseBody = sendToAI(prompt, aiInfo.getAiApiKey());
        if (responseBody == null) {
            throw new RuntimeException("Lỗi khi gọi AI API để tạo Postman Collection");
        }

        // Trích xuất JSON từ response body
        String collectionJson = extractJsonFromResponse(responseBody);
        // Kiểm tra xem JSON có được trích xuất thành công không
        if (collectionJson == null || collectionJson.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy JSON trong phần phản hồi.");
        }

        // 2. Gọi newman để kiểm tra chạy thành công
        // boolean isNewmanRunSuccessful = runNewman(collectionJson);
        // if (!isNewmanRunSuccessful) {
        //     throw new RuntimeException("Newman run failed.");
        // }
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
        postmanForGradingRepository.save(postmanForGrading);
        return "Postman Collection được tạo và lưu thành công.";
    }

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

    // Hàm chạy newman và kiểm tra kết quả
    private String runNewman(String collectionJson) {
        String postmanFunctionName = null;
        try {
            // Ghi collection JSON vào file tạm thời
            Path tempFile = Files.createTempFile("collection", ".json");
            Files.write(tempFile, collectionJson.getBytes(StandardCharsets.UTF_8));

            String newmanPath = "C:\\Users\\Admin\\AppData\\Roaming\\npm\\newman.cmd"; // Hoặc .exe nếu cần
            String timeout = "1000"; // Đặt thời gian chờ

            // Gọi newman bằng ProcessBuilder với tùy chọn timeout
            ProcessBuilder processBuilder = new ProcessBuilder(newmanPath, "run", tempFile.toAbsolutePath().toString(),
                    "--timeout", timeout);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder outputBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");

                    // Tìm postmanFunctionName từ chuỗi có dấu '→'
                    if (line.contains("→")) {
                        postmanFunctionName = line.substring(line.indexOf("→") + 1).trim();
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0 || outputBuilder.toString().contains("executed")) {
                return postmanFunctionName; // Trả về postmanFunctionName nếu thành công
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // private boolean runNewman(String collectionJson) {
    // try {
    // // Ghi collection JSON vào file tạm thời
    // Path tempFile = Files.createTempFile("collection", ".json");
    // Files.write(tempFile, collectionJson.getBytes(StandardCharsets.UTF_8));

    // // Đường dẫn đến Newman
    // String newmanPath = "C:\\Users\\Admin\\AppData\\Roaming\\npm\\newman.cmd"; //
    // Hoặc .exe nếu cần
    // String timeout = "1000"; // Đặt thời gian chờ

    // // Gọi newman bằng ProcessBuilder với tùy chọn timeout
    // ProcessBuilder processBuilder = new ProcessBuilder(newmanPath, "run",
    // tempFile.toAbsolutePath().toString(), "--timeout", timeout);
    // processBuilder.redirectErrorStream(true);
    // Process process = processBuilder.start();

    // // Đọc và lưu kết quả chạy newman vào StringBuilder
    // StringBuilder outputBuilder = new StringBuilder();
    // try (BufferedReader reader = new BufferedReader(
    // new java.io.InputStreamReader(process.getInputStream()))) {
    // String line;
    // while ((line = reader.readLine()) != null) {
    // outputBuilder.append(line).append("\n");
    // }
    // }

    // // Lưu kết quả vào file D:\Desktop\result.txt
    // try (BufferedWriter writer = new BufferedWriter(new
    // FileWriter("D:\\Desktop\\result.txt"))) {
    // writer.write(outputBuilder.toString());
    // }

    // int exitCode = process.waitFor();
    // // Nếu exitCode là 0 hoặc có ít nhất một request được thực hiện thì coi là
    // thành công
    // return exitCode == 0 || outputBuilder.toString().contains("executed");

    // } catch (Exception e) {
    // e.printStackTrace();
    // return false;
    // }
    // }

    // private boolean runNewman(String collectionJson) {
    // try {
    // // Ghi collection JSON vào file tạm thời
    // java.nio.file.Path tempFile =
    // java.nio.file.Files.createTempFile("collection", ".json");
    // java.nio.file.Files.write(tempFile,
    // collectionJson.getBytes(StandardCharsets.UTF_8));

    // // Đường dẫn đến Newman
    // String newmanPath = "C:\\Users\\Admin\\AppData\\Roaming\\npm\\newman.cmd"; //
    // Hoặc .exe nếu cần

    // String timeout = "1000"; // Đặt thời gian chờ

    // // Gọi newman bằng ProcessBuilder với tùy chọn timeout
    // ProcessBuilder processBuilder = new ProcessBuilder(newmanPath, "run",
    // tempFile.toAbsolutePath().toString(), "--timeout", timeout);
    // processBuilder.redirectErrorStream(true);
    // Process process = processBuilder.start();

    // // Đọc kết quả chạy newman
    // StringBuilder outputBuilder = new StringBuilder(); // Khởi tạo StringBuilder
    // để ghi lại đầu ra
    // try (java.io.BufferedReader reader = new java.io.BufferedReader(
    // new java.io.InputStreamReader(process.getInputStream()))) {
    // String line;
    // while ((line = reader.readLine()) != null) {
    // outputBuilder.append(line).append("\n"); // Ghi lại kết quả chạy
    // System.out.println(line); // In ra console
    // }
    // }

    // int exitCode = process.waitFor();
    // // Nếu exitCode là 0 hoặc có ít nhất một request được thực hiện thì coi là
    // thành công
    // if (exitCode == 0 || outputBuilder.toString().contains("executed")) {
    // return true;
    // } else {
    // // Bạn có thể kiểm tra thêm các thông điệp lỗi cụ thể nếu cần
    // System.out.println("Newman run encountered errors.");
    // return false;
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // return false;
    // }
    // }

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
                .findByExamQuestion_ExamPaper_ExamPaperId(examPaperId);

        return postmanForGradingEntries.stream()
                .map(entry -> {
                    PostmanForGradingDTO dto = new PostmanForGradingDTO();
                    dto.setPostmanForGradingId(entry.getPostmanForGradingId());
                    dto.setPostmanFunctionName(entry.getPostmanFunctionName());
                    dto.setScoreOfFunction(entry.getScoreOfFunction());
                    dto.setTotalPmTest(entry.getTotalPmTest());
                    dto.setOrderBy(entry.getOrderBy());
                    dto.setPostmanForGradingParentId(entry.getPostmanForGradingParentId()); // Lấy từ thực thể
                    dto.setExamQuestionId(entry.getExamQuestion().getExamQuestionId()); // Lấy từ thực thể
                    dto.setGherkinScenarioId(
                            entry.getGherkinScenario() != null ? entry.getGherkinScenario().getGherkinScenarioId()
                                    : null); // Lấy từ thực thể

                    return dto;
                })
                .collect(Collectors.toList());
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

}
