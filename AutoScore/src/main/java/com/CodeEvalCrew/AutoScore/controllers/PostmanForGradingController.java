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

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.GradingRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.services.postman_for_grading_service.IPostmanForGradingService;

@RestController
@RequestMapping("/api/postman-grading")
public class PostmanForGradingController {

    @Autowired
    private IPostmanForGradingService postmanForGradingService;

    @PutMapping("")
    public ResponseEntity<String> updatePostmanForGrading(@RequestBody PostmanForGradingUpdateRequest request) {
        String result = postmanForGradingService.updatePostmanForGrading(request.getExamPaperId(),
                request.getUpdateDTOs());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<PostmanForGradingDTO>> getPostmanForGrading_forFunctionTree(
            @RequestParam Long examPaperId) {
        List<PostmanForGradingDTO> postmanForGradingList = postmanForGradingService
                .getPostmanForGradingByExamPaperId(examPaperId);
        return new ResponseEntity<>(postmanForGradingList, HttpStatus.OK);
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generatePostmanCollection(@RequestParam Long gherkinScenarioId) {
        try {
            return postmanForGradingService.generatePostmanCollection(gherkinScenarioId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/generate-more")
    public ResponseEntity<?> generatePostmanCollectionMore(@RequestParam Long gherkinScenarioId) {
        try {
            return postmanForGradingService.generatePostmanCollectionMore(gherkinScenarioId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/merge/{examPaperId}")
    public ResponseEntity<String> mergePostmanCollections(@PathVariable Long examPaperId) {
        try {
            String message = postmanForGradingService.mergePostmanCollections(examPaperId);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("")
    public ResponseEntity<String> deletePostmanForGrading(@RequestParam List<Long> postmanForGradingIds) {
        String response = postmanForGradingService.deletePostmanForGrading(postmanForGradingIds);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> PostmanForGradingGetbyIdDTO(@PathVariable Long id) {
        try {
            return postmanForGradingService.getPostmanForGradingById(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/update-exam-question/{postmanForGradingId}/{examQuestionId}")
    public ResponseEntity<String> updateExamQuestionId(
            @PathVariable Long postmanForGradingId,
            @PathVariable Long examQuestionId) {
        // Call your service to update the exam question
        String result = postmanForGradingService.updateExamQuestionId(postmanForGradingId, examQuestionId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/calculate-scores")
    public ResponseEntity<?> calculateScores(
            @RequestParam("examPaperId") Long examPaperId,
            @RequestParam("examQuestionId") Long examQuestionId,
            @RequestBody List<GradingRequestDTO> requests) {
        try {
            // Pass examPaperId along with the requests to the service
            return postmanForGradingService.calculateScores(requests, examPaperId, examQuestionId);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid input: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + ex.getMessage());
        }
    }

}
