package com.CodeEvalCrew.AutoScore.services.gherkin_scenario_service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CreateGherkinScenarioDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinPostmanPairDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinScenarioDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinScenarioResponseDTO;

public interface IGherkinScenarioService {

    ResponseEntity<?> generateGherkinFormat(Long examQuestionId);

    ResponseEntity<String> generateGherkinFormatMore(List<Long> gherkinIds, Long examQuestionId);
  

    // List<GherkinScenarioDTO> getAllGherkinScenariosByExamPaperId(Long examPaperId);

    List<GherkinPostmanPairDTO> getAllGherkinAndPostmanPairs(Long examPaperId);

    List<GherkinPostmanPairDTO> getAllGherkinAndPostmanPairsByQuestionId(Long questionId);
   
    String deleteGherkinScenario(List<Long> gherkinScenarioIds, Long examquestionId);

    GherkinScenarioResponseDTO createGherkinScenario(CreateGherkinScenarioDTO dto);

    GherkinScenarioDTO getById(Long gherkinScenarioId);

    GherkinScenarioResponseDTO updateGherkinScenarios(Long gherkinScenarioId, String gherkinData);
  

}
