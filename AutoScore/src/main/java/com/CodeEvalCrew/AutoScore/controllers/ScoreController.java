package com.CodeEvalCrew.AutoScore.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.CodePlagiarismResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreCategoryDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreDetailsResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreOverViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentScoreDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.TopStudentDTO;
import com.CodeEvalCrew.AutoScore.services.score_service.IScoreService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/score")
public class ScoreController {

    @Autowired
    private IScoreService scoreService;

    @PreAuthorize("hasAnyAuthority('VIEW_SCORE', 'ALL_ACCESS')")
    @PostMapping
    public ResponseEntity<List<ScoreResponseDTO>> getScoreJSON(@RequestParam Long exampaperid) {
        try {
            List<ScoreResponseDTO> scoreResponseDTOs = scoreService.getScoresByExamPaperId(exampaperid);
            return ResponseEntity.ok(scoreResponseDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('EXPORT_SCORE', 'ALL_ACCESS')")
    @GetMapping("/export")
    public void exportScoresToExcel(HttpServletResponse response, @RequestParam Long exampaperid) {
        try {
            scoreService.exportScoresToExcel(response, exampaperid);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_SCORE', 'ALL_ACCESS')")
    @GetMapping("/getAll")
    public ResponseEntity<List<ScoreOverViewResponseDTO>> getAllScoreOverView() {
        try {
            List<ScoreOverViewResponseDTO> scores = scoreService.getScoreOverView();
            return ResponseEntity.ok(scores);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_SCORE', 'ALL_ACCESS')")
    @PostMapping("/details")
    public ResponseEntity<List<ScoreDetailsResponseDTO>> getScoreDetailsByScoreId(@RequestParam Long scoreId) {
        try {
            List<ScoreDetailsResponseDTO> scoreDetailResponseDTOs = scoreService.getScoreDetailsByScoreId(scoreId);
            return ResponseEntity.ok(scoreDetailResponseDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_SCORE', 'ALL_ACCESS')")
    @GetMapping("/code-plagiarism")
    public ResponseEntity<List<CodePlagiarismResponseDTO>> getCodePlagiarismByScoreId(@RequestParam Long scoreId) {
        try {
            List<CodePlagiarismResponseDTO> response = scoreService.getCodePlagiarismByScoreId(scoreId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/total-students")
    public ResponseEntity<Integer> getTotalStudents(@RequestParam Long examPaperId) {
        try {
            int totalStudents = scoreService.getTotalStudentsByExamPaperId(examPaperId);
            return ResponseEntity.ok(totalStudents);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/students-with-zero-score")
    public ResponseEntity<Integer> getTotalStudentsWithZeroScore(@RequestParam Long examPaperId) {
        try {
            int totalStudents = scoreService.getTotalStudentsWithZeroScore(examPaperId);
            return ResponseEntity.ok(totalStudents);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/students-with-score-greater-than-zero")
    public ResponseEntity<Integer> getTotalStudentsWithScoreGreaterThanZero(@RequestParam Long examPaperId) {
        try {
            int totalStudents = scoreService.getTotalStudentsWithScoreGreaterThanZero(examPaperId);
            return ResponseEntity.ok(totalStudents);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/student-scores")
    public ResponseEntity<List<StudentScoreDTO>> getStudentScoresByExamPaperId(@RequestParam Long examPaperId) {
        try {
            List<StudentScoreDTO> studentScores = scoreService.getStudentScoresByExamPaperId(examPaperId);
            return ResponseEntity.ok(studentScores);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/top-students")
    public List<TopStudentDTO> getTopStudents() {
        return scoreService.getTopStudents();
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/total-score-occurrences")
    public ResponseEntity<Map<Float, Long>> getTotalScoreOccurrences() {
        Map<Float, Long> scoreOccurrences = scoreService.getTotalScoreOccurrences();
        return ResponseEntity.ok(scoreOccurrences);
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/score-categories")
    public ResponseEntity<ScoreCategoryDTO> getScoreCategories() {
        ScoreCategoryDTO categories = scoreService.getScoreCategories();
        return ResponseEntity.ok(categories);
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/analyze-log")
    public List<Map<String, Object>> analyzeLog() {
        return scoreService.analyzeLog();
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/analyze-log-all-pass")
    public ResponseEntity<?> analyzeScoresFullyPassLogRunPostman(@RequestParam Long examPaperId) {
        try {
            Map<String, Integer> functionPassCount = scoreService.analyzeScoresFullyPassLogRunPostman(examPaperId);
            return ResponseEntity.ok(functionPassCount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/analyze-log-one-pass")
    public ResponseEntity<?> analyzeScoresPartialPassLogRunPostman(@RequestParam Long examPaperId) {
        try {
            Map<String, Integer> partialPassCounts = scoreService.analyzeScoresPartialPassLogRunPostman(examPaperId);
            return ResponseEntity.ok(partialPassCounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/analyze-log-fail-all")
    public ResponseEntity<?> analyzeScoresFailedAllTests(@RequestParam Long examPaperId) {
        try {
            Map<String, Integer> functionFailCounts = scoreService.analyzeScoresFailedAllTests(examPaperId);
            return ResponseEntity.ok(functionFailCounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/get-total-run-and-average-response-time")
    public ResponseEntity<?> getTotalRunAndAverageResponseTime(@RequestParam Long examPaperId) {
        try {
            Map<String, Map<String, Double>> totalAndAverageResponseTimes = scoreService
                    .getTotalRunAndAverageResponseTime(examPaperId);
            return ResponseEntity.ok(totalAndAverageResponseTimes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("/get-code-plagiarism-details")
    public ResponseEntity<?> getCodePlagiarismDetails(@RequestParam Long examPaperId) {
        try {

            List<Map<String, String>> plagiarismDetails = scoreService
                    .getCodePlagiarismDetailsByExamPaperId(examPaperId);
            return ResponseEntity.ok(plagiarismDetails);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}
