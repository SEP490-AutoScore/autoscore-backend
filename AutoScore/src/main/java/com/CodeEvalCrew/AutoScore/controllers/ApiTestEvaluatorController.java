package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Testcase.TestCase;
import com.CodeEvalCrew.AutoScore.services.testcase_service.IApiTestEvaluator;

import io.swagger.v3.oas.annotations.Operation;
import net.minidev.json.JSONObject;

@RestController
@RequestMapping("/api/test-evaluator")
public class ApiTestEvaluatorController {

    @Autowired
    private IApiTestEvaluator apiTestEvaluator;

    @PostMapping
    @Operation(summary = "Evaluate API test cases")
    public JSONObject evaluateTestCases(@RequestParam("studentId") Long studentId,
            @RequestParam("examPaperId") Long examPaperId,
            @RequestParam("port") String port,
            @RequestParam("tokenPath") String tokenPath,
            @RequestBody List<TestCase> testCases) throws Exception {
                
        JSONObject jsonObject = apiTestEvaluator.runTestCases(testCases, port, studentId, examPaperId, tokenPath);
        return jsonObject;
    }
}
