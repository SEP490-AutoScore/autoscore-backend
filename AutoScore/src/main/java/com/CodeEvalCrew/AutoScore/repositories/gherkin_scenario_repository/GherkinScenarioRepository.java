package com.CodeEvalCrew.AutoScore.repositories.gherkin_scenario_repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;

@Repository
public interface GherkinScenarioRepository extends JpaRepository<Gherkin_Scenario, Long>{
    List<Gherkin_Scenario> findByExamQuestion_ExamQuestionIdOrderByOrderPriorityAsc(Long examQuestionId);

}