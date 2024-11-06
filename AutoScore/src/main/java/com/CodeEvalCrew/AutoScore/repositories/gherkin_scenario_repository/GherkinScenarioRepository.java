package com.CodeEvalCrew.AutoScore.repositories.gherkin_scenario_repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;

@Repository
public interface GherkinScenarioRepository extends JpaRepository<Gherkin_Scenario, Long>{
    
}
