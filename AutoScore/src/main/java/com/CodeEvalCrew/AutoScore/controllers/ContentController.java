package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ContentDTO;
import com.CodeEvalCrew.AutoScore.services.content_service.IContentService;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private IContentService contentService;

    @PreAuthorize("hasAnyAuthority('VIEW_PROMPT_AI', 'ALL_ACCESS')")
    @GetMapping("")
    public List<ContentDTO> getAllContent() {
        return contentService.getAllContent();
    }

    @PreAuthorize("hasAnyAuthority('EDIT_PROMPT_AI', 'ALL_ACCESS')")
    @PutMapping("/{contentId}/question")
    public String updateQuestionAskAiContent(
            @PathVariable Long contentId,
            @RequestBody String newQuestionAskAiContent) {
        contentService.updateQuestionAskAiContent(contentId, newQuestionAskAiContent);
        return "Update success";
    }
}