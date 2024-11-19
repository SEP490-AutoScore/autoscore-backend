package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.GherkinScenario.GenerateGherkinFormatDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinScenarioDTO;
import com.CodeEvalCrew.AutoScore.services.gherkin_scenario_service.IGherkinScenarioService;

@RestController
@RequestMapping("api/gherkin_scenario")
public class GherkinScenarioController {

    @Autowired
    private IGherkinScenarioService gherkinScenarioService;

      @GetMapping("/all")
    public ResponseEntity<List<GherkinScenarioDTO>> getAllByExamPaperId(@RequestParam Long examPaperId) {
        List<GherkinScenarioDTO> result = gherkinScenarioService.getAllGherkinScenariosByExamPaperId(examPaperId);
        return ResponseEntity.ok(result);
 
    }
    @PostMapping("/generate_gherkin_format")
    public ResponseEntity<String> generateGherkinFormat(@RequestBody GenerateGherkinFormatDTO request) {
        String result = gherkinScenarioService.generateGherkinFormat(request.getExamQuestionIds());
        return ResponseEntity.ok(result);
    }

     
    @GetMapping("/questionId")
    public ResponseEntity<List<GherkinScenarioDTO>> getAllGherkinScenarios(@RequestParam Long examQuestionId) {
        List<GherkinScenarioDTO> scenarios = gherkinScenarioService.getAllGherkinScenariosByExamQuestionId(examQuestionId);
        return ResponseEntity.ok(scenarios);
    }

    @PutMapping("")
    public ResponseEntity<String> updateGherkinScenarios(@RequestParam Long examQuestionId,
                                                         @RequestBody String gherkinDataBody) {
        gherkinScenarioService.updateGherkinScenarios(examQuestionId, gherkinDataBody);
        return ResponseEntity.ok("Gherkin Scenarios updated successfully.");
    }
}
