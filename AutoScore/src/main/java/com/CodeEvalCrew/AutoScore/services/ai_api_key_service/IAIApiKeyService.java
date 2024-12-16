package com.CodeEvalCrew.AutoScore.services.ai_api_key_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CreateAIApiKeyDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIApiKeyDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.AIName_Enum;

public interface IAIApiKeyService {
    List<AIApiKeyDTO> getAllAIApiKeys();

    void updateOrCreateAccountSelectedKey(Long aiApiKeyId);

    void createAIApiKey(CreateAIApiKeyDTO dto);

    void updateAI_Api_Key(Long aiApiKeyId, boolean shared);

    boolean deleteAIApiKey(Long aiApiKeyId);

    AIApiKeyDTO getAIApiKeyById(Long aiApiKeyId);

    List<AIName_Enum> getAllAINameEnums();

}