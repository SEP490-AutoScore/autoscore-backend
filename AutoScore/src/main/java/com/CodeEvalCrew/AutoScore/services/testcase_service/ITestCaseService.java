package com.CodeEvalCrew.AutoScore.services.testcase_service;

public interface ITestCaseService {
    String getAIResponse(Long examDatabaseId, Long examBaremId, int minimumNumberOfTestcases);
}
