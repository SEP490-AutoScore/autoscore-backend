package com.CodeEvalCrew.AutoScore.repositories.AI_prompt_reposotiry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.CodeEvalCrew.AutoScore.models.Entity.AI_Prompt;

public interface  IAIPromptRepository extends JpaRepository<AI_Prompt, Long>,JpaSpecificationExecutor<AI_Prompt> {
    
}
