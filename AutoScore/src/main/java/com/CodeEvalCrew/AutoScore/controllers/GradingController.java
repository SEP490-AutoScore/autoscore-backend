package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Grading.GradingRequest;
import com.CodeEvalCrew.AutoScore.services.grading_service.IGradingService;

@RestController
@RequestMapping("api/grading")
public class GradingController {

    @Autowired
    private IGradingService gradingService;

    @PostMapping("")
    public ResponseEntity<?> startGradingProcess(@RequestBody GradingRequest request) {
        try {
            gradingService.startingGradingProcess(request);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
