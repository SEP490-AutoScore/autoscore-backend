package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.GherkinScenario.GenerateGherkinFormatDTO;
import com.CodeEvalCrew.AutoScore.services.gherkin_scenario_service.IGherkinScenarioService;

@RestController
@RequestMapping("api/gherkin_scenario")
public class GherkinScenarioController {

    @Autowired
    private IGherkinScenarioService gherkinScenarioService;

    @PostMapping("/generate_gherkin_format")
    public ResponseEntity<String> generateGherkinFormat(@RequestBody GenerateGherkinFormatDTO request) {
        String result = gherkinScenarioService.generateGherkinFormat(request.getExamQuestionIds());
        return ResponseEntity.ok(result);
    }

     
    @GetMapping("")
    public ResponseEntity<String> getAllGherkinScenarios(@RequestParam Long examQuestionId) {
        String combinedGherkinData = gherkinScenarioService.getAllGherkinScenariosByExamQuestionId(examQuestionId);
        return ResponseEntity.ok(combinedGherkinData);
    }
}
