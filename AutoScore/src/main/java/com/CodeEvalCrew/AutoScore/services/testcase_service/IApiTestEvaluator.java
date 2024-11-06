package com.CodeEvalCrew.AutoScore.services.testcase_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Testcase.TestCase;

import net.minidev.json.JSONObject;

public interface IApiTestEvaluator {
    JSONObject runTestCases(List<TestCase> testCases, String port, Long studentId, Long examPaperId, String tokenPath) throws Exception;
}
