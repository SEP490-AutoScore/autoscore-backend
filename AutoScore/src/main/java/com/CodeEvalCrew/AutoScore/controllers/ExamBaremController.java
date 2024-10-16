package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem.ExamBaremCreate;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem.ExamBaremViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamBaremView;
import com.CodeEvalCrew.AutoScore.services.exam_barem_service.IExamBaremService;

@RestController
@RequestMapping("api/barem")
public class ExamBaremController {

    @Autowired
    private final IExamBaremService examBaremService;

    public ExamBaremController(IExamBaremService examBaremService) {
        this.examBaremService = examBaremService;
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getById(@RequestParam Long id) {
        try {

            ExamBaremView result = examBaremService.getExamById(id);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage(), HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getCause());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createNewExamBarem(@RequestBody ExamBaremCreate request) {
        ExamBaremView result;
        try {

            result = examBaremService.createNewExamBarem(request);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage() ,HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateExamBarem(@PathVariable Long id, @RequestBody ExamBaremCreate request) {
        ExamBaremView result;
        try {

            result = examBaremService.updateExamBarem(id, request);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage() ,HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("list")
    public ResponseEntity<?> getList(@RequestBody ExamBaremViewRequest request) {
        List<ExamBaremView> result;
        try {

            result = examBaremService.getList(request);
            if (!result.isEmpty()) {
                throw new NoSuchElementException("No exam barem match!");
            }

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException nse) {
            return new ResponseEntity<>(nse.getMessage(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
