package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CreateGherkinScenarioDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinPostmanPairDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinScenarioDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinScenarioResponseDTO;
import com.CodeEvalCrew.AutoScore.services.gherkin_scenario_service.IGherkinScenarioService;

@RestController
@RequestMapping("api/gherkin_scenario")
public class GherkinScenarioController {

    @Autowired
    private IGherkinScenarioService gherkinScenarioService;

    @GetMapping("/pairs")
    public ResponseEntity<List<GherkinPostmanPairDTO>> getAllGherkinAndPostmanPairs(@RequestParam Long examPaperId) {
        List<GherkinPostmanPairDTO> result = gherkinScenarioService.getAllGherkinAndPostmanPairs(examPaperId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pairs/by-question")
    public ResponseEntity<List<GherkinPostmanPairDTO>> getAllGherkinAndPostmanPairsByQuestionId(
            @RequestParam Long questionId) {
        List<GherkinPostmanPairDTO> result = gherkinScenarioService
                .getAllGherkinAndPostmanPairsByQuestionId(questionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<GherkinScenarioDTO>> getAllByExamPaperId(@RequestParam Long examPaperId) {
        List<GherkinScenarioDTO> result = gherkinScenarioService.getAllGherkinScenariosByExamPaperId(examPaperId);
        return ResponseEntity.ok(result);

    }

    @PostMapping("/generate_gherkin_format")
    public ResponseEntity<String> generateGherkinFormat(@RequestParam Long examQuestionId) {
        String result = gherkinScenarioService.generateGherkinFormat(examQuestionId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate_gherkin_format_more")
    public ResponseEntity<String> generateGherkinFormatMore(@RequestParam Long examQuestionId) {
        String result = gherkinScenarioService.generateGherkinFormatMore(examQuestionId);
        return ResponseEntity.ok(result);
    }

    @PutMapping(value = "/{gherkinScenarioId}", consumes = "multipart/form-data")
    public ResponseEntity<GherkinScenarioResponseDTO> updateGherkinData(
            @PathVariable Long gherkinScenarioId,
            @RequestParam("gherkinData") String gherkinData) {
        try {
            GherkinScenarioResponseDTO responseDTO = gherkinScenarioService.updateGherkinScenarios(gherkinScenarioId, gherkinData);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }
    
    

    @DeleteMapping("/{gherkinScenarioId}")
    public ResponseEntity<GherkinScenarioResponseDTO> deleteGherkinScenario(@PathVariable Long gherkinScenarioId) {
        try {
            // Gọi service để xóa (cập nhật trạng thái) Gherkin Scenario và trả về DTO
            GherkinScenarioResponseDTO responseDTO = gherkinScenarioService.deleteGherkinScenario(gherkinScenarioId);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("")
    public ResponseEntity<GherkinScenarioResponseDTO> createGherkinScenario(@RequestBody CreateGherkinScenarioDTO dto) {
        try {
            // Gọi service để tạo mới Gherkin Scenario và trả về DTO
            GherkinScenarioResponseDTO responseDTO = gherkinScenarioService.createGherkinScenario(dto);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{gherkinScenarioId}")
    public ResponseEntity<GherkinScenarioDTO> getById(@PathVariable Long gherkinScenarioId) {
        GherkinScenarioDTO gherkinScenarioDTO = gherkinScenarioService.getById(gherkinScenarioId);
        return ResponseEntity.ok(gherkinScenarioDTO);
    }

}
