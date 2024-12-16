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

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.services.postman_for_grading_service.IPostmanForGradingService;

@RestController
@RequestMapping("/api/postman-grading")
public class PostmanForGradingController {

    @Autowired
    private IPostmanForGradingService postmanForGradingService;

    @PreAuthorize("hasAnyAuthority('UPDATE_POSTMAN', 'ALL_ACCESS')")
    @PutMapping("")
    public ResponseEntity<String> updatePostmanForGrading(@RequestBody PostmanForGradingUpdateRequest request) {
        String result = postmanForGradingService.updatePostmanForGrading(request.getExamPaperId(),
                request.getUpdateDTOs());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('VIEW_GHERKIN_POSTMAN', 'ALL_ACCESS')")
    @GetMapping("")
    public ResponseEntity<List<PostmanForGradingDTO>> getPostmanForGrading_forFunctionTree(
            @RequestParam Long examPaperId) {
        List<PostmanForGradingDTO> postmanForGradingList = postmanForGradingService
                .getPostmanForGradingByExamPaperId(examPaperId);
        return new ResponseEntity<>(postmanForGradingList, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('GENERATE_POSTMAN', 'ALL_ACCESS')")
    @PostMapping("/generate")
    public ResponseEntity<?> generatePostmanCollection(@RequestParam Long gherkinScenarioId) {
        try {
            return postmanForGradingService.generatePostmanCollection(gherkinScenarioId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('GENERATE_POSTMAN', 'ALL_ACCESS')")
    @PostMapping("/generate-more")
    public ResponseEntity<?> generatePostmanCollectionMore(@RequestParam Long postmanForGradingId) {
        try {
            return postmanForGradingService.generatePostmanCollectionMore(postmanForGradingId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('MERGE_POSTMAN', 'ALL_ACCESS')")
    @PostMapping("/merge/{examPaperId}")
    public ResponseEntity<String> mergePostmanCollections(@PathVariable Long examPaperId) {
        try {
            String message = postmanForGradingService.mergePostmanCollections(examPaperId);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('DELETE_POSTMAN', 'ALL_ACCESS')")
    @DeleteMapping("")
    public ResponseEntity<String> deletePostmanForGrading(@RequestParam List<Long> postmanForGradingIds,
            Long examPaperId) {
        String response = postmanForGradingService.deletePostmanForGrading(postmanForGradingIds, examPaperId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('VIEW_GHERKIN_POSTMAN', 'ALL_ACCESS')")
    @GetMapping("/{id}")
    public ResponseEntity<?> PostmanForGradingGetbyIdDTO(@PathVariable Long id) {
        try {
            return postmanForGradingService.getPostmanForGradingById(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_QUESTION_POSTMAN', 'ALL_ACCESS')")
    @PutMapping("/update-exam-question/{postmanForGradingId}/{examQuestionId}")
    public ResponseEntity<String> updateExamQuestionId(
            @PathVariable Long postmanForGradingId,
            @PathVariable Long examQuestionId) {
        String result = postmanForGradingService.updateExamQuestionId(postmanForGradingId, examQuestionId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/calculate")
    public ResponseEntity<String> calculateScores(@RequestParam Long examPaperId) {
        try {
            postmanForGradingService.calculateScores(examPaperId);
            return ResponseEntity.ok("Scores calculated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}
