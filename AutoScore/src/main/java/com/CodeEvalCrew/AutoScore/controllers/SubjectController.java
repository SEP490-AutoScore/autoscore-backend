package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.CreateSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.UpdateSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import com.CodeEvalCrew.AutoScore.services.subject_service.ISubjectService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/subject")
public class SubjectController {

    @Autowired
    private ISubjectService subjectService;

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') and hasAuthority('VIEW_SUBJECT')")
    @GetMapping("/{subjectCode}")
    public Page<Subject> getSubjectByCode(
            @PathVariable String subjectCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Assuming the service method can handle pagination and return a List
        return subjectService.getSubjectByCode(subjectCode, PageRequest.of(page, size));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') and hasAuthority('VIEW_SUBJECT')")
    @GetMapping("")
    public Page<Subject> getAllSubjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return subjectService.getAllSubjects(PageRequest.of(page, size));
    }


    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') and hasAuthority('DELETE_SUBJECT')")
    @DeleteMapping("{id}")
    public void deleteSubject(@PathVariable long id) {
        subjectService.deleteSubject(id);
    }
      @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') and hasAuthority('CREATE_SUBJECT')")
    @PostMapping("")
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        Subject createdSubject = subjectService.createSubject(request);
        return new ResponseEntity<>(createdSubject, HttpStatus.CREATED);
    }
    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') and hasAuthority('UPDATE_SUBJECT')")
    @PutMapping("")
    public ResponseEntity<Subject> updateSubject(@Valid @RequestBody UpdateSubjectRequest request) {
        Subject updatedSubject = subjectService.updateSubject(request);
        return new ResponseEntity<>(updatedSubject, HttpStatus.OK);
    }
    

}
