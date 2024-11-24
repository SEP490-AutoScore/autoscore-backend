package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.services.source_grading_service.ISourceGradingService;

import org.springframework.web.bind.annotation.GetMapping;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceView;


@RestController
@RequestMapping("api/source")
public class SourceController {
    @Autowired
    private ISourceGradingService sourceGradingService;

    @GetMapping("")
    public ResponseEntity<?> getAllSource() {
        List<SourceView> result;
        try {
            result = sourceGradingService.getAllSource();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT) ;
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR) ;
        }
    }
    
}
