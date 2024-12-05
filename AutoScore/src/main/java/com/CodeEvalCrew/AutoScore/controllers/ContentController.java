package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ContentDTO;
import com.CodeEvalCrew.AutoScore.services.content_service.ContentService;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    @PreAuthorize("hasAnyAuthority('ALL_ACCESS')")
    @GetMapping("")
    public List<ContentDTO> getAllContent() {
        return contentService.getAllContent();
    }
}