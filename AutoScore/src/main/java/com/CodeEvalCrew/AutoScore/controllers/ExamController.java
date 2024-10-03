package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.CodeEvalCrew.AutoScore.models.DTO.ReponseEntity;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.services.exam_service.IExamService;


@RestController
@RequestMapping("api/exam")
public class ExamController {
    private final IExamService examService;

    public ExamController(IExamService examService) {
        this.examService = examService;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @GetMapping("{id}")
    public ReponseEntity<Exam> getExamById(@PathVariable long id) {
        ReponseEntity<Exam> result = new ReponseEntity<>();
        try {
            result = examService.getExamById(id);
            return result;
        } catch (Exception e) {
            result.error(e.getMessage());
            // return new ReponseEntity<Exam>().ok();
            return result;
        }
    }
}
