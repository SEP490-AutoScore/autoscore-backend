package com.CodeEvalCrew.AutoScore.services.ai_api_key_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CreateAIApiKeyDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIApiKeyDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Api_Key;

public interface IAIApiKeyService {
    List<AIApiKeyDTO> getAllAIApiKeys();

    void updateOrCreateAccountSelectedKey(Long aiApiKeyId);

    AIApiKeyDTO createAIApiKey(CreateAIApiKeyDTO dto);

      AI_Api_Key updateAiApiKey(Long aiApiKeyId, String aiApiKey);

     void deleteAiApiKey(Long aiApiKeyId);
}