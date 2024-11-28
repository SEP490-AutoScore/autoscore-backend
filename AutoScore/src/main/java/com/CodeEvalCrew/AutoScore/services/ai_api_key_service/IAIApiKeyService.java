package com.CodeEvalCrew.AutoScore.services.ai_api_key_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CreateAIApiKeyDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIApiKeyDTO;


public interface IAIApiKeyService {
    List<AIApiKeyDTO> getAllAIApiKeys();

    void updateOrCreateAccountSelectedKey(Long aiApiKeyId);

    AIApiKeyDTO createAIApiKey(CreateAIApiKeyDTO dto);

    // String  updateAiApiKey(Long aiApiKeyId, String aiApiKey);

    void updateAI_Api_Key(Long aiApiKeyId, String aiApiKey);
    
     void deleteAiApiKey(Long aiApiKeyId);
}