package com.CodeEvalCrew.AutoScore.services.ai_prompt_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.AIPrompt.CreatePromptDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIPromptView;

public interface IAIPromptService {

    AIPromptView getPromptById(long id) throws Exception;

    List<AIPromptView> getPromptByUser(long id);

    AIPromptView createNewPrompt(CreatePromptDTO request);
    
}
