package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIApiKeyDTO;
import com.CodeEvalCrew.AutoScore.services.ai_api_key_service.IAIApiKeyService;

@RestController
@RequestMapping("api/ai_api_keys")
public class AIApiKeyController {

    @Autowired
    private IAIApiKeyService aiApiKeyService;

    @GetMapping("")
    public ResponseEntity<List<AIApiKeyDTO>> getAllAIApiKeys() {
        List<AIApiKeyDTO> apiKeys = aiApiKeyService.getAllAIApiKeys();
        return ResponseEntity.ok(apiKeys);
    }
}
