package com.CodeEvalCrew.AutoScore.services.ai_prompt_service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.AIPrompt.CreatePromptDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIPromptView;

@Service
public interface IAIPromptService {

    AIPromptView getPromptById(long id) throws Exception;

    List<AIPromptView> getPromptByUser(long id) throws Exception;

    AIPromptView createNewPrompt(CreatePromptDTO request) throws Exception;

    AIPromptView updatePrompt(CreatePromptDTO request) throws Exception;
    
}
