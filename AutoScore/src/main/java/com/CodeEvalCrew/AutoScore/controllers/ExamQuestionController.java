package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;
import java.util.NoSuchElementException;

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
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamQuestionView;
import com.CodeEvalCrew.AutoScore.services.exam_question_service.IExamQuestionService;

@RestController
@RequestMapping("api/exam-question")
public class ExamQuestionController {

    @Autowired
    private final IExamQuestionService examQuestionService;

    public ExamQuestionController(IExamQuestionService examQuestionService) {
        this.examQuestionService = examQuestionService;
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ExamQuestionView result;
        try {
            
            result = examQuestionService.getById(id);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage() ,HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("list")
    public ResponseEntity<?> getListExamQuestion(@RequestBody ExamQuestionViewRequest request) {
        List<ExamQuestionView> result;
        try {

            result = examQuestionService.getList(request);
            
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NoSuchElementException nse) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage() ,HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createExamQeustion(@RequestBody ExamQuestionCreateRequest request) {
        ExamQuestionView result;
        try {
            
            result = examQuestionService.createNewExamQuestion(request);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage() ,HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateExamQuestion(@PathVariable Long id, @RequestBody ExamQuestionCreateRequest request) {
        ExamQuestionView result;
        try {
            result = examQuestionService.updateExamQuestion(id, request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nse) {
            return new ResponseEntity<>(nse.getMessage(),HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteExamQuestion(Long id) {
        ExamQuestionView result;
        try {
            result = examQuestionService.deleteExamQuestion(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nse) {
            return new ResponseEntity<>(nse.getMessage(),HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
