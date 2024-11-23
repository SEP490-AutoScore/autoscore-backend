package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreOverViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreResponseDTO;
import com.CodeEvalCrew.AutoScore.services.score_service.IScoreService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/score")
public class ScoreController {

    @Autowired
    private IScoreService scoreService;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'EXAMINER') or hasAuthority('VIEW_SCORE')")
    @PostMapping
    public ResponseEntity<List<ScoreResponseDTO>> getScoreJSON(@RequestParam Long exampaperid) {
        try {
            List<ScoreResponseDTO> scoreResponseDTOs = scoreService.getScoresByExamPaperId(exampaperid);
            return ResponseEntity.ok(scoreResponseDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'EXAMINER') or hasAuthority('EXPORT_SCORE')")
    @GetMapping("/export")
    public void exportScoresToExcel(HttpServletResponse response, @RequestParam Long exampaperid) {
        try {
            List<ScoreResponseDTO> scores = scoreService.getScoresByExamPaperId(exampaperid);
            scoreService.exportScoresToExcel(response, scores);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'EXAMINER') or hasAuthority('VIEW_SCORE')")
    @GetMapping("/getAll")
    public ResponseEntity<List<ScoreOverViewResponseDTO>> getAllScoreOverView() {
        try {
            List<ScoreOverViewResponseDTO> scores = scoreService.getScoreOverView();
            return ResponseEntity.ok(scores);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
