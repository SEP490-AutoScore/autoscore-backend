package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamViewRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;
import com.CodeEvalCrew.AutoScore.services.exam_service.IExamService;

@RestController
@RequestMapping("api/exam/")
public class ExamController {

    private final IExamService examService;

    public ExamController(IExamService examService) {
        this.examService = examService;
    }

    @PreAuthorize("hasAuthority('VIEW_EXAM')")
    @GetMapping("{id}")
    public ResponseEntity<?> getExamById(@PathVariable long id) {
        ExamViewResponseDTO result;
        try {
            result = examService.getExamById(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EXAMINER','ROLE_HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @PostMapping("/list")
    public ResponseEntity<?> getExam(@RequestBody ExamViewRequestDTO request) {
        List<ExamViewResponseDTO> result;
        try {
            // call service to get result
            result = examService.GetExam(request);
        } catch (NoSuchElementException nsee) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            // return notfound
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @PostMapping("")
    public ResponseEntity<?> creatNewExam(@RequestBody ExamCreateRequestDTO entity) {
        try {
            //call service for create new exam
            ExamViewResponseDTO result = examService.createNewExam(entity);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExam(@PathVariable Long id, @RequestBody ExamCreateRequestDTO request) {
        try {
            //call service for update exam
            ExamViewResponseDTO result = examService.updateExam(request, id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
