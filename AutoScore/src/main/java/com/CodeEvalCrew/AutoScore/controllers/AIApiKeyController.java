package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

     @PostMapping("/update-selected-key")
    public ResponseEntity<String> updateSelectedKey(@RequestParam Long aiApiKeyId) {
        try {
            aiApiKeyService.updateOrCreateAccountSelectedKey(aiApiKeyId);
            return ResponseEntity.ok("AI API Key selection updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating.");
        }
    }
}
