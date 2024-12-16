package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamViewRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamWithPapersDTO;
import com.CodeEvalCrew.AutoScore.services.exam_service.IExamService;

@RestController
@RequestMapping("api/exam/")
public class ExamController {

    private final IExamService examService;

    public ExamController(IExamService examService) {
        this.examService = examService;
    }

    @PreAuthorize("hasAnyAuthority('VIEW_EXAM', 'ALL_ACCESS')")
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

    @PreAuthorize("hasAnyAuthority('VIEW_EXAM', 'ALL_ACCESS')")
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

    @PreAuthorize("hasAnyAuthority('CREATE_EXAM', 'ALL_ACCESS')")
    @PostMapping("")
    public ResponseEntity<?> creatNewExam(@RequestBody ExamCreateRequestDTO entity) {
        try {
            // call service for create new exam
            ExamViewResponseDTO result = examService.createNewExam(entity);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PreAuthorize("hasAnyAuthority('CREATE_EXAM', 'ALL_ACCESS')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExam(@PathVariable Long id, @RequestBody ExamCreateRequestDTO request) {
        try {
            // call service for update exam
            ExamViewResponseDTO result = examService.updateExam(request, id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("list-exam-exampaper")
    public ResponseEntity<List<ExamWithPapersDTO>> getExamsWithUsedPapers() throws NotFoundException {
        try {
            List<ExamWithPapersDTO> result = examService.getExamWithUsedPapers();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("count")
    public ResponseEntity<Object> getExamCountByTypeAndCampus() {
        try {
            long count = examService.countExamsByTypeAndCampus();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (IllegalArgumentException e) {

            return new ResponseEntity<>("Authenticated user does not belong to any CAMPUS.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {

            return new ResponseEntity<>("An internal server error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("countByGradingAt")
    public ResponseEntity<Long> getExamCountByTypeAndGradingAt() {
        try {
            long count = examService.countExamsByTypeAndGradingAt();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("countByGradingAtPassed")
    public ResponseEntity<Long> getExamCountByGradingAtPassed() {
        try {
            long count = examService.countExamsByGradingAtPassed();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('DASHBOARD', 'ALL_ACCESS')")
    @GetMapping("countByGradingAtPassedAndSemester")
    public ResponseEntity<Map<String, Long>> getExamCountByGradingAtPassedAndSemester(@RequestParam int year) {
        try {

            Map<String, Long> examCounts = examService.countExamsByGradingAtPassedAndSemester(year);
            return new ResponseEntity<>(examCounts, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
