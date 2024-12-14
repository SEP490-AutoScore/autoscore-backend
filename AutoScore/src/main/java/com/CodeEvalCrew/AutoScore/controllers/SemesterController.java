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
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.CreateSemesterRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.SemesterView;
import com.CodeEvalCrew.AutoScore.services.semesterService.ISemesterService;


@RestController
@RequestMapping("api/semester")
public class SemesterController {
    @Autowired
    private ISemesterService semesterService;

    @PreAuthorize("hasAnyAuthority('ALL_ACCESS')")
    @GetMapping("")
    public ResponseEntity<?> getAllSemester() {
        List<SemesterView> result;
        try {
            result = semesterService.getAllSemester();
            return new ResponseEntity<>(result,HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('ALL_ACCESS')")
    @PostMapping("")
    public ResponseEntity<?> createNewSemester(@RequestBody CreateSemesterRequest request) {
        SemesterView result;
        try {
            result = semesterService.createNewSemester(request);
            return new ResponseEntity<>(result,HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("{semesterId}")
    public ResponseEntity<?> updateSemesterInfo(@RequestBody CreateSemesterRequest request, @PathVariable Long semesterId) {
        SemesterView result;
        try {
            result = semesterService.updateSemesterInfo(semesterId, request);
            return new ResponseEntity<>(result,HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
