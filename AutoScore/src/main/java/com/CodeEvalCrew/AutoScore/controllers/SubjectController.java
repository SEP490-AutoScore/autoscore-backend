package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.CreateSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SubjectView;
import com.CodeEvalCrew.AutoScore.services.subject_service.ISubjectService;

@RestController
@RequestMapping("api/subject")
public class SubjectController {

    @Autowired
    private ISubjectService subjectService;

    @PreAuthorize("hasAnyAuthority('VIEW_SUBJECT', 'ALL_ACCESS')")
    @GetMapping("")
    public ResponseEntity<?> getSubject() {
        List<SubjectView> result;
        try {
            result = subjectService.getAllSubject();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('CREATE_SUBJECT', 'ALL_ACCESS')")
    @PostMapping("")
    public ResponseEntity<?> createNewSubject(@RequestBody CreateSubjectRequest request) {
        SubjectView result;
        try {
            result = subjectService.createNewSubject(request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_SUBJECT', 'ALL_ACCESS')")
    @PutMapping("org")
    public ResponseEntity<?> addSubjectIntoOrganiztion(@RequestParam Long organizationId, @RequestParam Long subjectId) {
        SubjectView result;
        try {
            result = subjectService.addSubjectintoOrganization(organizationId, subjectId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_SUBJECT', 'ALL_ACCESS')")
    @PutMapping("{subjectId}")
    public ResponseEntity<?> updateInfoSubject(@RequestBody CreateSubjectRequest request,@PathVariable Long subjectId) {
        SubjectView result;
        try {
            result = subjectService.updateInfoSubject(subjectId, request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_SUBJECT', 'ALL_ACCESS')")
    @GetMapping("{subjectId}")
    public ResponseEntity<?> getSubjectBySubjectId(@PathVariable Long subjectId) {
        SubjectView result;
        try {
            result = subjectService.getSubjectBySubjectId(subjectId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
