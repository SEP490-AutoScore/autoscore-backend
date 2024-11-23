package com.CodeEvalCrew.AutoScore.controllers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceDetailDTO;
import com.CodeEvalCrew.AutoScore.services.source_service.SourceDetailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/source-details")
public class SourceDetailController {

    @Autowired
    private SourceDetailService sourceDetailService;

    @GetMapping
    public List<SourceDetailDTO> getAllSourceDetails() {
        return sourceDetailService.getAllSourceDetails();
    }

    @GetMapping("/{id}")
    public SourceDetailDTO getSourceDetailById(@PathVariable Long id) {
        return sourceDetailService.getSourceDetailById(id);
    }

    @PostMapping
    public SourceDetailDTO createSourceDetail(@RequestBody SourceDetailDTO dto) {
        return sourceDetailService.createSourceDetail(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSourceDetail(@PathVariable Long id) {
        sourceDetailService.deleteSourceDetail(id);
    }
}