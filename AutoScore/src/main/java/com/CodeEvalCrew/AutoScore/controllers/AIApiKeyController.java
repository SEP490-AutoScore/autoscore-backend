package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CreateAIApiKeyDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIApiKeyDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.AIName_Enum;
import com.CodeEvalCrew.AutoScore.services.ai_api_key_service.IAIApiKeyService;

@RestController
@RequestMapping("api/ai_api_keys")
public class AIApiKeyController {

    @Autowired
    private IAIApiKeyService aiApiKeyService;

    @PreAuthorize("hasAnyAuthority('VIEW_API_KEY', 'ALL_ACCESS')")
    @GetMapping("")
    public ResponseEntity<List<AIApiKeyDTO>> getAllAIApiKeys() {
        List<AIApiKeyDTO> apiKeys = aiApiKeyService.getAllAIApiKeys();
        return ResponseEntity.ok(apiKeys);
    }

    @PreAuthorize("hasAnyAuthority('VIEW_API_KEY', 'ALL_ACCESS')")
    @GetMapping("/{aiApiKeyId}")
    public ResponseEntity<AIApiKeyDTO> getAIApiKeyById(@PathVariable Long aiApiKeyId) {
        AIApiKeyDTO aiApiKeyDTO = aiApiKeyService.getAIApiKeyById(aiApiKeyId);
        return ResponseEntity.ok(aiApiKeyDTO);
    }

    @PreAuthorize("hasAnyAuthority('SELECT_OTHER_KEY', 'ALL_ACCESS')")
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

    @PreAuthorize("hasAnyAuthority('CREATE_API_KEY', 'ALL_ACCESS')")
    @PostMapping("")
    public ResponseEntity<String> createAIApiKey(@RequestBody CreateAIApiKeyDTO dto) {
        try {
            aiApiKeyService.createAIApiKey(dto);
            return new ResponseEntity<>("Create successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create API Key", HttpStatus.BAD_REQUEST);
        }
    }
    

    @PreAuthorize("hasAnyAuthority('VIEW_API_KEY', 'ALL_ACCESS')")
    @PutMapping("/{aiApiKeyId}")
    public ResponseEntity<Void> updateAIApiKey(
            @PathVariable Long aiApiKeyId,
            @RequestParam boolean shared)  {
        try {
            aiApiKeyService.updateAI_Api_Key(aiApiKeyId, shared);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when update", ex);
        }
    }

    @PreAuthorize("hasAnyAuthority('DELETE_API_KEY', 'ALL_ACCESS')")
    @DeleteMapping("/{aiApiKeyId}")
    public ResponseEntity<String> deleteAIApiKey(@PathVariable Long aiApiKeyId) {
        boolean isDeleted = aiApiKeyService.deleteAIApiKey(aiApiKeyId);

        if (isDeleted) {
            return ResponseEntity.ok("Delete successful");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Delete failed");
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_API_KEY', 'ALL_ACCESS')")
    @GetMapping("/ai-names")
    public List<AIName_Enum> getAllAINames() {
        // Call the service to get the enum values
        return aiApiKeyService.getAllAINameEnums();
    }

}
