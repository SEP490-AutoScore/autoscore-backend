package com.CodeEvalCrew.AutoScore.services.testcase_service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Testcase.TestCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

@Service
public class ApiTestEvaluator implements IApiTestEvaluator {

    private Long studentId;
    private Long examPaperId;
    private Long examBaremId;
    private int totalScore = 0;
    private String tokenPath = null;
    private String port = null;
    private final List<Map<String, Object>> logs = new ArrayList<>();
    private String authToken = null;
    private final String AUTHENTICATION_SUCCESS = "Authentication successfully.";
    private final String AUTHENTICATION_FAILED = "Authentication failed.";
    private final String ACCESS_DENIED = "Access denied.";
    private final String SUCCESS = "There is a data response, the data response matches with expected status and body.";
    private final String FAILED = "No data response or data response doesn't match with expected status and body.";

    @Override
    public JSONObject runTestCases(List<TestCase> testCases, String port, Long studentId, Long examPaperId, String tokenPath) throws Exception {
        this.totalScore = 0;
        this.logs.clear();
        this.studentId = studentId;
        this.examPaperId = examPaperId;
        this.port = port;
        if (tokenPath == null || tokenPath.isEmpty()) {
            throw new IllegalArgumentException("Token path is null or empty");
        }        
        this.tokenPath = tokenPath;

        for (TestCase testCase : testCases) {
            this.authToken = null;
            this.examBaremId = testCase.getExamBaremId();

            // Kiểm tra nếu test case này là login
            if (isLoginTestCase(testCase)) {
                HttpResponse<String> loginResponse = authenticate(testCase);

                // Kiểm tra login có thành công không
                boolean loginSuccess = checkExpectedResponse(testCase, loginResponse);
                if (!loginSuccess) {
                    addLog(testCase.getTestCaseName(), AUTHENTICATION_FAILED, false, 0);
                    totalScore = 0;
                    return generateResultJson(); // Login thất bại, trả về 0 điểm
                }
                authToken = extractTokenFromResponse(loginResponse);  // Lưu token để sử dụng cho các test case khác
                int score = testCase.getScore();
                totalScore += score;
                addLog(testCase.getTestCaseName(), AUTHENTICATION_SUCCESS, true, testCase.getScore());
            } else {
                // Kiểm tra quyền truy cập của role trước khi thực hiện test case
                if (!testRoleAccess(testCase)) {
                    addLog(testCase.getTestCaseName(), ACCESS_DENIED, false, 0);
                    totalScore = 0;
                    return generateResultJson(); // Nếu không có quyền, trả về 0 điểm và dừng lại
                }

                // Thực hiện gọi API
                HttpResponse<String> response = callApi(testCase);

                // Kiểm tra kết quả
                boolean passed = checkExpectedResponse(testCase, response);
                int score = passed ? testCase.getScore() : 0;

                // Thay đổi cách cộng điểm ở đây
                if (passed) {
                    totalScore += score; // Chỉ cộng điểm nếu test case thành công
                    addLog(testCase.getTestCaseName(), SUCCESS, passed, score); // Ghi lại log
                } else {
                    addLog(testCase.getTestCaseName(), FAILED, passed, score);
                }
            }
        }

        // Tạo file JSON log trả về kết quả
        return generateResultJson();
    }

    // Hàm kiểm tra xem test case có phải là login không
    private boolean isLoginTestCase(TestCase testCase) {
        return testCase.isLoginFunction();
    }

    // Hàm đăng nhập và trả về token
    private String extractTokenFromResponse(HttpResponse<String> response) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        
        // Sử dụng tokenPath để lấy token
        String[] pathSegments = tokenPath.split("\\.");
        JsonNode tokenNode = jsonResponse;
    
        for (String segment : pathSegments) {
            tokenNode = tokenNode.path(segment);
            if (tokenNode.isMissingNode()) {
                return null; // Nếu không tìm thấy token theo đường dẫn, trả về null
            }
        }
    
        return tokenNode.asText(); // Trả về token
    }

    // Hàm kiểm tra role có quyền truy cập vào API
    private boolean testRoleAccess(TestCase testCase) throws Exception {
        // Kiểm tra nếu test case yêu cầu xác thực và token chưa tồn tại
        if (testCase.isRequiresAuth() && authToken == null) {
            // Gọi API đăng nhập và lưu token
            HttpResponse<String> authResponse = authenticate(testCase);
            if (authResponse.statusCode() == 401) {
                return false; // Không có quyền
            }
            
            // Sử dụng tokenPath để lấy token từ JSON response
            authToken = extractTokenFromResponse(authResponse);
            
            // Kiểm tra nếu không lấy được token
            if (authToken == null) {
                return false; // Không có token trong JSON response
            }
        }
        // Nếu không yêu cầu xác thực hoặc token đã tồn tại, tiếp tục test case
        return true;
    }    

    // Hàm gọi API dựa trên test case
    private HttpResponse<String> callApi(TestCase testCase) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // Xây dựng URI cho path parameters và query parameters
        String finalUri = port + testCase.getPath();
        if (testCase.getQueryParams() != null && !testCase.getQueryParams().isEmpty()) {
            StringBuilder queryParams = new StringBuilder();
            for (Map.Entry<String, String> entry : testCase.getQueryParams().entrySet()) {
                if (queryParams.length() == 0) {
                    queryParams.append("?");
                } else {
                    queryParams.append("&");
                }
                queryParams.append(entry.getKey()).append("=").append(entry.getValue());
            }
            finalUri += queryParams.toString();
        }

        // Khởi tạo builder cho request
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(finalUri))
                .header("Content-Type", "application/json")
                .method(testCase.getMethod(), HttpRequest.BodyPublishers.noBody());  // Mặc định không có body

        // Thêm token vào headers nếu có
        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        // Nếu method là POST, PUT hoặc PATCH thì thêm body vào request
        if (testCase.getMethod().equalsIgnoreCase("POST")
                || testCase.getMethod().equalsIgnoreCase("PUT")
                || testCase.getMethod().equalsIgnoreCase("PATCH")) {
            requestBuilder.method(testCase.getMethod(), HttpRequest.BodyPublishers.ofString(testCase.getBody()));
        }

        // Thực hiện request và trả về response
        return client.send(requestBuilder.build(), BodyHandlers.ofString());
    }

    // Kiểm tra response có đúng với mong đợi không
    private boolean checkExpectedResponse(TestCase testCase, HttpResponse<String> response) {
        try {
            // Kiểm tra status code trước
            if (response.statusCode() != testCase.getExpectedStatusCode()) {
                return false;
            }

            // Parse expected và actual body thành JsonNode để dễ dàng so sánh key
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode expectedBody = objectMapper.readTree(testCase.getExpectedResponse());
            JsonNode actualBody = objectMapper.readTree(response.body());

            // Gọi hàm đệ quy để kiểm tra tất cả các key
            return compareJsonNodes(expectedBody, actualBody);

        } catch (JsonProcessingException e) {
            return false;
        }
    }

    // Hàm đệ quy để kiểm tra các key của expectedBody và actualBody
    private boolean compareJsonNodes(JsonNode expectedNode, JsonNode actualNode) {
        // Lặp qua tất cả các key trong expectedNode
        Iterator<String> fieldNames = expectedNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();

            // Nếu actualNode không có key này, trả về false
            if (!actualNode.has(fieldName)) {
                return false;
            }

            // Nếu key này là một object, thì tiếp tục đệ quy kiểm tra sâu hơn
            JsonNode expectedChildNode = expectedNode.get(fieldName);
            JsonNode actualChildNode = actualNode.get(fieldName);

            if (expectedChildNode.isObject()) {
                // Nếu là object, gọi đệ quy để kiểm tra key trong object con
                if (!compareJsonNodes(expectedChildNode, actualChildNode)) {
                    return false;
                }
            }
        }

        // Nếu tất cả các key đều khớp, trả về true
        return true;
    }

    // Hàm thêm log vào danh sách
    private void addLog(String testCaseName, String feedback, boolean passed, int score) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("TestCaseName", testCaseName);
        logEntry.put("Feedback", feedback);
        logEntry.put("IsPass", passed);
        logEntry.put("BaremScore", score);
        logEntry.put("GradedAt", LocalDateTime.now());
        logEntry.put("ExamBaremId", examBaremId);
        logs.add(logEntry);
    }

    // Tạo file JSON kết quả
    private JSONObject generateResultJson() throws Exception {
        JSONObject result = new JSONObject();
        result.put("ExamPaperId", examPaperId);
        result.put("StudentId", studentId);
        result.put("TotalScore", totalScore);
        result.put("GradedAt", LocalDateTime.now());

        // Chuyển đổi logs thành JSONArray
        JSONArray logArray = new JSONArray();
        for (Map<String, Object> logEntry : logs) {
            JSONObject logObj = new JSONObject(logEntry);
            logArray.add(logObj);
        }

        result.put("ScoreDetails", logArray);
        return result;
    }

    // Hàm giả lập đăng nhập và kiểm tra role (giả định)
    private HttpResponse<String> authenticate(TestCase testCase) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(port +testCase.getAuthPath()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(testCase.getAuthBody()))
                .build();

        return client.send(request, BodyHandlers.ofString());
    }
}
