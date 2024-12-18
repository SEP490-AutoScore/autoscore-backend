package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAnyAuthority('VIEW_GHERKIN_POSTMAN', 'ALL_ACCESS')")
    @GetMapping("/pairs")
    public ResponseEntity<List<GherkinPostmanPairDTO>> getAllGherkinAndPostmanPairs(@RequestParam Long examPaperId) {
        List<GherkinPostmanPairDTO> result = gherkinScenarioService.getAllGherkinAndPostmanPairs(examPaperId);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyAuthority('VIEW_GHERKIN_POSTMAN', 'ALL_ACCESS')")
    @GetMapping("/pairs/by-question")
    public ResponseEntity<List<GherkinPostmanPairDTO>> getAllGherkinAndPostmanPairsByQuestionId(
            @RequestParam Long questionId) {
        List<GherkinPostmanPairDTO> result = gherkinScenarioService
                .getAllGherkinAndPostmanPairsByQuestionId(questionId);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyAuthority('GENERATE_GHERKIN_SCENARIO', 'ALL_ACCESS')")
    @PostMapping("/generate_gherkin_format")
    public ResponseEntity<?> generateGherkinFormat(@RequestParam Long examQuestionId) {
        return gherkinScenarioService.generateGherkinFormat(examQuestionId);
    }

    @PreAuthorize("hasAnyAuthority('GENERATE_GHERKIN_SCENARIO', 'ALL_ACCESS')")
    @PostMapping("/generate_gherkin_format_more")
    public ResponseEntity<String> generateGherkinFormatMore(
            @RequestBody List<Long> gherkinIds, @RequestParam Long examQuestionId) {
        return gherkinScenarioService.generateGherkinFormatMore(gherkinIds, examQuestionId);
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_GHERKIN_SCENARIO', 'ALL_ACCESS')")
    @PutMapping(value = "/{gherkinScenarioId}", consumes = "multipart/form-data")
    public ResponseEntity<GherkinScenarioResponseDTO> updateGherkinData(
            @PathVariable Long gherkinScenarioId,
            @RequestParam("gherkinData") String gherkinData) {
        try {
            GherkinScenarioResponseDTO responseDTO = gherkinScenarioService.updateGherkinScenarios(gherkinScenarioId,
                    gherkinData);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    @PreAuthorize("hasAnyAuthority('DELETE_GHERKIN_SCENARIO', 'ALL_ACCESS')")
    @DeleteMapping("/gherkinScenarioIds")
    public ResponseEntity<String> deleteGherkinScenarios(@RequestParam List<Long> gherkinScenarioIds,
            @RequestParam Long examPaperId) {
        try {

            String result = gherkinScenarioService.deleteGherkinScenario(gherkinScenarioIds, examPaperId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting Gherkin Scenarios.", HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasAnyAuthority('CREATE_GHERKIN_SCENARIO', 'ALL_ACCESS')")
    @PostMapping("")
    public ResponseEntity<GherkinScenarioResponseDTO> createGherkinScenario(@RequestBody CreateGherkinScenarioDTO dto) {
        try {

            GherkinScenarioResponseDTO responseDTO = gherkinScenarioService.createGherkinScenario(dto);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_GHERKIN_POSTMAN', 'ALL_ACCESS')")
    @GetMapping("/{gherkinScenarioId}")
    public ResponseEntity<GherkinScenarioDTO> getById(@PathVariable Long gherkinScenarioId) {
        GherkinScenarioDTO gherkinScenarioDTO = gherkinScenarioService.getById(gherkinScenarioId);
        return ResponseEntity.ok(gherkinScenarioDTO);
    }

}
