package com.CodeEvalCrew.AutoScore.services.aiprompt_service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIPromptDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Prompt;
import com.CodeEvalCrew.AutoScore.repositories.aiprompt_repository.AIPromptRepository;

@Service
public class AIPromptService implements IAIPromptService  {

    @Autowired
    private AIPromptRepository aipromptRepository;

    @Override
    public List<AIPromptDTO> getAllAIPrompt() {
     
        List<AI_Prompt> aipromptList = aipromptRepository.findAllByOrderByPurposeAscOrderPriorityAsc();

        return aipromptList.stream()
                .map(aiprompt -> new AIPromptDTO(
                        aiprompt.getAiPromptId(),
                        aiprompt.getQuestionAskAiContent(),
                        aiprompt.getOrderPriority(),
                        aiprompt.getPurpose()
                ))
                .collect(Collectors.toList());
    }

    @Override
      public void updateQuestionAskAiContent(Long aiPromptId, String newQuestionAskAiContent) {

        AI_Prompt aiPrompt = aipromptRepository.findById(aiPromptId)
                .orElseThrow(() -> new IllegalArgumentException("AI Prompt not found with ID: " + aiPromptId));

                aiPrompt.setQuestionAskAiContent(newQuestionAskAiContent);

        aipromptRepository.save(aiPrompt);
    }
}