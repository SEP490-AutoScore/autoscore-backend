package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.services.postman_for_grading_service.IPostmanForGradingService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/postman-grading")
public class PostmanForGradingController {

    @Autowired
    private IPostmanForGradingService postmanForGradingService;

    @GetMapping("/{examPaperId}")
    public ResponseEntity<List<PostmanForGradingDTO>> getPostmanForGrading(@PathVariable Long examPaperId) {
        List<PostmanForGradingDTO> postmanForGradingList = postmanForGradingService.getPostmanForGradingByExamPaperId(examPaperId);
        return new ResponseEntity<>(postmanForGradingList, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updatePostmanForGradingList(@RequestBody List<PostmanForGradingDTO> postmanForGradingDTOs) {
        postmanForGradingService.updatePostmanForGradingList(postmanForGradingDTOs);
        return new ResponseEntity<>(HttpStatus.OK);
 
    }
}
