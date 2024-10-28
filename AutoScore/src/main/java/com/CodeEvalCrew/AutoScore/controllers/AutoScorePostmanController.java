package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.StudentSourceInfoDTO;
import com.CodeEvalCrew.AutoScore.services.autoscore_postman_service.IAutoscorePostmanService;

@RestController
@RequestMapping("/api/autoscore-postman")
public class AutoScorePostmanController {

    @Autowired
    private IAutoscorePostmanService autoscorePostmanService;

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') or hasAuthority('VIEW_EXAM')")
    @GetMapping("")
    public List<StudentSourceInfoDTO> getStudentSourceInfo(
        @RequestParam Long examPaperId,
        @RequestParam(name = "numberOfAssignmentsDeployedAtTheSameTime", required = false, defaultValue = "3") int numberOfAssignmentsDeployedAtTheSameTime
    ) {
        return autoscorePostmanService.getStudentSourceInfoByExamPaperId(examPaperId, numberOfAssignmentsDeployedAtTheSameTime);
    }
}
