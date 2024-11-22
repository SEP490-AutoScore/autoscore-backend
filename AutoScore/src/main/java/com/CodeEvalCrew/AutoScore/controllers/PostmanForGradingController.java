package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

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
        String result = postmanForGradingService.updatePostmanForGrading(request.getExamPaperId(), request.getUpdateDTOs());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @GetMapping("")
    public ResponseEntity<List<PostmanForGradingDTO>> getPostmanForGrading_forFunctionTree(
            @RequestParam Long examPaperId) {
        List<PostmanForGradingDTO> postmanForGradingList = postmanForGradingService
                .getPostmanForGradingByExamPaperId(examPaperId);
        return new ResponseEntity<>(postmanForGradingList, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updatePostmanForGradingList(
            @RequestBody List<PostmanForGradingDTO> postmanForGradingDTOs) {
        postmanForGradingService.updatePostmanForGradingList(postmanForGradingDTOs);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/generate/{gherkinScenarioId}")
    public ResponseEntity<String> generatePostmanCollection(@PathVariable Long gherkinScenarioId) {
        try {
            String resultMessage = postmanForGradingService.generatePostmanCollection(gherkinScenarioId);
            return new ResponseEntity<>(resultMessage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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

}
