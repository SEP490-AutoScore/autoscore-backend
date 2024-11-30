package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Api_Key;
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

    @PostMapping("")
    public ResponseEntity<AIApiKeyDTO> createAIApiKey(@RequestBody CreateAIApiKeyDTO dto) {
        try {
            AIApiKeyDTO response = aiApiKeyService.createAIApiKey(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

//     @PutMapping("/updateKey")
// public ResponseEntity<String> updateAiApiKey(
//         @RequestParam Long aiApiKeyId,
//         @RequestParam String aiApiKey) {

//     try {
//         // Cập nhật API Key qua Service và nhận thông điệp từ service
//         String responseMessage = aiApiKeyService.updateAiApiKey(aiApiKeyId, aiApiKey);

//         // Trả về thông điệp thành công
//         return new ResponseEntity<>(responseMessage, HttpStatus.OK);
//     } catch (ResponseStatusException e) {
//         // Trả về thông điệp lỗi nếu không thể cập nhật
//         return new ResponseEntity<>(e.getReason(), e.getStatusCode());
//     }
// }

@PutMapping("/{aiApiKeyId}")
public ResponseEntity<Void> updateAIApiKey(
        @PathVariable Long aiApiKeyId,
        @RequestParam String aiApiKey) {
    try {
        aiApiKeyService.updateAI_Api_Key(aiApiKeyId, aiApiKey);
        return ResponseEntity.noContent().build(); // Trả về 204 nếu cập nhật thành công
    } catch (ResponseStatusException ex) {
        // Ném lại ResponseStatusException để trả mã lỗi chính xác
        throw ex;
    } catch (Exception ex) {
        // Xử lý các lỗi không mong muốn
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi cập nhật API Key", ex);
    }
}

    

    @DeleteMapping("/{aiApiKeyId}")
    public ResponseEntity<String> deleteAiApiKey(@PathVariable Long aiApiKeyId) {
        try {
            // Gọi hàm delete trong service để cập nhật status = false
            aiApiKeyService.deleteAiApiKey(aiApiKeyId);
            return ResponseEntity.ok("AI API Key status updated to false.");
        } catch (Exception e) {
            // Nếu có lỗi, trả về thông báo lỗi
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("AI API Key not found.");
        }
    }
}
