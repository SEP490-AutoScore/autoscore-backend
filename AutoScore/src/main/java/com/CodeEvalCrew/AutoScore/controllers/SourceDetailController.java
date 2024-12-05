package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceDetailDTO;
import com.CodeEvalCrew.AutoScore.services.source_service.SourceDetailService;

@RestController
@RequestMapping("/api/source-details")
public class SourceDetailController {

    @Autowired
    private SourceDetailService sourceDetailService;

    @PreAuthorize("hasAnyAuthority('ALL_ACCESS')")
    @GetMapping
    public List<SourceDetailDTO> getAllSourceDetails() {
        return sourceDetailService.getAllSourceDetails();
    }
}