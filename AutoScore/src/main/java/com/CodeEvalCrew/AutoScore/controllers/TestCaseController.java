package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.services.testcase_service.ITestCaseService;

@RestController
@RequestMapping("/api/test-case")
public class TestCaseController {

    @Autowired
    private ITestCaseService testCaseService;

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') or hasAuthority('CREATE_TESTCASE')")
    @GetMapping("/prompt")
    public String getResponse(@RequestParam Long examDatabaseId) {
        return testCaseService.getAIResponse(examDatabaseId);
    }
}
