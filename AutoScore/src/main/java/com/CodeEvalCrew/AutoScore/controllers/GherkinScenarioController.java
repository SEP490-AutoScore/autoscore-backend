package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.services.gherkin_scenario_service.IGherkinScenarioService;

@RestController
@RequestMapping("api/gherkin_scenario")
public class GherkinScenarioController {

    private final IGherkinScenarioService gherkinScenarioService;

    @Autowired
    public GherkinScenarioController(IGherkinScenarioService gherkinScenarioService) {
        this.gherkinScenarioService = gherkinScenarioService;
    }

    @GetMapping("/generate_gherkin_format")
    public ResponseEntity<String> generateGherkinFormat(@RequestParam Long examQuestionId) {
        String result = gherkinScenarioService.generateGherkinFormat(examQuestionId);
        return ResponseEntity.ok(result);
    }
}
