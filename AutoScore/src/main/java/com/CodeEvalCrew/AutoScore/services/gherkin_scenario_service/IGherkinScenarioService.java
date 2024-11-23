package com.CodeEvalCrew.AutoScore.services.gherkin_scenario_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinPostmanPairDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinScenarioDTO;

public interface IGherkinScenarioService {
    // String generateGherkinFormat(Long examQuestionId);
    String generateGherkinFormat(List<Long> examQuestionIds);

    // String getAllGherkinScenariosByExamQuestionId(Long examQuestionId);
    List<GherkinScenarioDTO> getAllGherkinScenariosByExamQuestionId(Long examQuestionId);

    void updateGherkinScenarios(Long examQuestionId, String gherkinDataBody);

    List<GherkinScenarioDTO> getAllGherkinScenariosByExamPaperId(Long examPaperId);

     List<GherkinPostmanPairDTO> getAllGherkinAndPostmanPairs(Long examPaperId);

     List<GherkinPostmanPairDTO> getAllGherkinAndPostmanPairsByQuestionId(Long questionId);
}
