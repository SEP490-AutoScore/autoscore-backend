package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIPromptDTO;
import com.CodeEvalCrew.AutoScore.services.aiprompt_service.IAIPromptService;

@RestController
@RequestMapping("/api/aiprompt")
public class AIPromptController {

    @Autowired
    private IAIPromptService aiPromptService;

    @PreAuthorize("hasAnyAuthority('VIEW_PROMPT_AI', 'ALL_ACCESS')")
    @GetMapping("")
    public List<AIPromptDTO> getAllAIPrompt() {
        return aiPromptService.getAllAIPrompt();
    }

    @PreAuthorize("hasAnyAuthority('EDIT_PROMPT_AI', 'ALL_ACCESS')")
    @PutMapping("/{aiPromptId}/question")
    public String updateQuestionAskAiContent(
            @PathVariable Long aiPromptId,
            @RequestBody String newQuestionAskAiContent) {
                aiPromptService.updateQuestionAskAiContent(aiPromptId, newQuestionAskAiContent);
        return "Update success";
    }

}