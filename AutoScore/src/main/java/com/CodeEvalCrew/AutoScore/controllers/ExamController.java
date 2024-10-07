package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("api/exam/")
public class ExamController {
    private final IExamService examService;

    public ExamController(IExamService examService) {
        this.examService = examService;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @GetMapping("{id}")
    public ResponseEntity<ExamViewResponseDTO> getExamById(@PathVariable long id){
        ExamViewResponseDTO result;
        try {
            result = examService.getExamById(id);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @PostMapping("")
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

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @PutMapping("")
    public ResponseEntity<ExamViewResponseDTO> creatNewExam(@RequestBody ExamCreateRequestDTO entity) {
        try{
            //call service for create new exam
            ExamViewResponseDTO result = examService.CreateNewExam(entity);

            
            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch (Exception ex){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @PutMapping("/{id}")
    public ResponseEntity<ExamViewResponseDTO> putMethodName(@PathVariable String id, @RequestBody ExamCreateRequestDTO request) {
        
        try{
            //call service for update exam
            ExamViewResponseDTO result = examService.updateExam(request);

            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch (Exception ex){
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }
        
    }
    
}
