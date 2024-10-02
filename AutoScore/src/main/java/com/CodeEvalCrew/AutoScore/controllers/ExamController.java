package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.services.exam_service.IExamService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;



@RestController
@RequestMapping("api/exam/")
public class ExamController {
    private final IExamService examService;

    public ExamController(IExamService examService) {
        this.examService = examService;
    }

    @GetMapping("{id}")
    public ResponseEntity<Exam> getExamById(@PathVariable long id){
        Exam result;
        result = examService.getExamById(id);
        if(result == null){
            //return not found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);                // throw new NotFoundException();
        }
        //return exam
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("exams")
    public ResponseEntity<Exam> getExam(@RequestBody long id) {
        Exam result;
        try {
            result = examService.getExamById(id);
            
        } catch (Exception e) {
            // return new ReponseEntity<Exam>().ok();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
            
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("new-exam")
    public ResponseEntity<ExamCreateRequestDTO> creatNewExam(@RequestBody ExamCreateRequestDTO entity) {
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
}
