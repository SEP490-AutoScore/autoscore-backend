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

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingCreateDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingGetDTO;
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

    @PostMapping("/generate")
    public ResponseEntity<String> generatePostmanCollection(@RequestParam Long gherkinScenarioId) {
        try {
            String resultMessage = postmanForGradingService.generatePostmanCollection(gherkinScenarioId);
            return new ResponseEntity<>(resultMessage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

    @PostMapping("/generate-more")
    public ResponseEntity<String> generatePostmanCollectionMore(@RequestParam Long gherkinScenarioId) {
        try {
            String resultMessage = postmanForGradingService.generatePostmanCollectionMore(gherkinScenarioId);
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

     @DeleteMapping("")
    public ResponseEntity<String> deletePostmanForGrading(@RequestParam Long postmanForGradingId) {
        String response = postmanForGradingService.deletePostmanForGrading(postmanForGradingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postmanForGradingId}")
    public ResponseEntity<PostmanForGradingGetDTO> getPostmanForGradingById(@PathVariable Long postmanForGradingId) {
        PostmanForGradingGetDTO dto = postmanForGradingService.getPostmanForGradingById(postmanForGradingId);
        return ResponseEntity.ok(dto);
    }
    
   

     @PostMapping
    public ResponseEntity<PostmanForGradingGetDTO> createPostmanForGrading(@RequestBody PostmanForGradingCreateDTO createDTO) {
        PostmanForGradingGetDTO newPostman = postmanForGradingService.createPostmanForGrading(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPostman);
    }
}
