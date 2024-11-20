package com.CodeEvalCrew.AutoScore.services.ai_info_service;

import com.CodeEvalCrew.AutoScore.models.Entity.AI_Info;
import com.CodeEvalCrew.AutoScore.repositories.ai_info_repository.AIInfoRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class AIInfoService implements IAIInfoService {

    @Autowired
    private AIInfoRepository aiInfoRepository;

    public void updateAiApiKey(String aiApiKey) {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        AI_Info aiInfo = aiInfoRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("AI_Info not found for authenticated user"));

        aiInfo.setAiApiKey(aiApiKey);
        aiInfo.setCreatedBy(authenticatedUserId);
        aiInfoRepository.save(aiInfo);
    }
}
