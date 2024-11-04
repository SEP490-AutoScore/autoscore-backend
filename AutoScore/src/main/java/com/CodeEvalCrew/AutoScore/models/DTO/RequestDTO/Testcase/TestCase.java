package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Testcase;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCase {
    @JsonProperty("test_case_name")
    private String testCaseName;

    @JsonProperty("path")
    private String path;  // Path chứa các thông số

    @JsonProperty("method")
    private String method;  // Method như GET, POST, PUT, DELETE

    @JsonProperty("body")
    private String body;  // Dùng cho POST, PUT

    @JsonProperty("expected_response")
    private String expectedResponse;

    @JsonProperty("expected_status_code")
    private int expectedStatusCode;

    @JsonProperty("score")
    private int score;

    @JsonProperty("requires_auth")
    private boolean requiresAuth;  // Kiểm tra quyền

    @JsonProperty("auth_path")
    private String authPath;  // Đường dẫn để xác thực

    @JsonProperty("auth_body")
    private String authBody;  // Nội dung của xác thực

    @JsonProperty("path_parameters")
    private Map<String, String> pathParameters;  // Thông số path

    @JsonProperty("query_params")
    private Map<String, String> queryParams;  // Các thông số query nếu cần

    @JsonProperty("is_login_function")
    private boolean isLoginFunction;

    @JsonProperty("exam_barem_id")
    private Long examBaremId;
}
