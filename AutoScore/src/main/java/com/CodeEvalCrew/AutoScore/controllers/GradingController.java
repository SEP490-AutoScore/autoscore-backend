package com.CodeEvalCrew.AutoScore.controllers;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Grading.GradingRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GradingProcessView;
import com.CodeEvalCrew.AutoScore.services.grading_service.IGradingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/ws/progress")
    public ResponseEntity<?> loadingProcess(@RequestParam Long examPaperId) {
        GradingProcessView result;
        try {
            result = gradingService.loadingGradingProgress(examPaperId);
            return new ResponseEntity<>(result ,HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
