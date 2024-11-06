package com.CodeEvalCrew.AutoScore.services.gherkin_scenario_service;
import java.util.List;

public interface IGherkinScenarioService {
    // String generateGherkinFormat(Long examQuestionId);
    String generateGherkinFormat(List<Long> examQuestionIds);
}
