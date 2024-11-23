package com.CodeEvalCrew.AutoScore.services.ai_api_key_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIApiKeyDTO;

public interface IAIApiKeyService {
    List<AIApiKeyDTO> getAllAIApiKeys();
}