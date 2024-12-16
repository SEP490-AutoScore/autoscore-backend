package com.CodeEvalCrew.AutoScore.services.aiprompt_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIPromptDTO;

public interface IAIPromptService {
    List<AIPromptDTO> getAllAIPrompt();

    void updateQuestionAskAiContent(Long aiPromptId, String newQuestionAskAiContent);
}
