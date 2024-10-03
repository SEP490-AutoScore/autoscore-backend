package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

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
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamViewRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;



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
        try {
            result = examService.getExamById(id);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("exams")
    public ResponseEntity<List<ExamViewResponseDTO>> getExam(@RequestBody ExamViewRequestDTO request) {
        List<ExamViewResponseDTO> result;
        try {
            // call service to get result
            result = examService.GetExam(request);
        } catch (Exception e) {
            // return notfound
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
            
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("new-exam")
    public ResponseEntity<ExamViewResponseDTO> creatNewExam(@RequestBody ExamCreateRequestDTO entity) {
        try{
            //call service for create new exam
            ExamViewResponseDTO result = examService.CreateNewExam(entity);

            
            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch (Exception ex){
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }
        
    }
    
}
