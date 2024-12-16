package com.CodeEvalCrew.AutoScore.repositories.aiprompt_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.AI_Prompt;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Purpose_Enum;

@Repository
public interface AIPromptRepository extends JpaRepository<AI_Prompt, Long> {

    List<AI_Prompt> findByPurposeOrderByOrderPriority(Purpose_Enum purpose);

    List<AI_Prompt> findAllByOrderByPurposeAscOrderPriorityAsc();
}
