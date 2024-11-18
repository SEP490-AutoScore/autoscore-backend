package com.CodeEvalCrew.AutoScore.repositories.gherkin_scenario_repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;

@Repository
public interface GherkinScenarioRepository extends JpaRepository<Gherkin_Scenario, Long>{
    List<Gherkin_Scenario> findByExamQuestion_ExamQuestionIdAndStatusTrueOrderByOrderPriorityAsc(Long examQuestionId);
    List<Gherkin_Scenario> findByExamQuestion_ExamQuestionId(Long examQuestionId);


}