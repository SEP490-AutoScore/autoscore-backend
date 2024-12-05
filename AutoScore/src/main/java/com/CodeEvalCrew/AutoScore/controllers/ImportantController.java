package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Important.GetImportantRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ImportantView;
import com.CodeEvalCrew.AutoScore.services.important_service.IImportantService;

@RestController
@RequestMapping("api/important")
public class ImportantController {

    @Autowired
    private IImportantService importantService;

    @PreAuthorize("hasAnyAuthority('ALL_ACCESS')")
    @GetMapping("")
    public ResponseEntity<?> getImportantOfSubject(@RequestParam Long subjectId) {
        List<ImportantView> result;
        GetImportantRequest request = new GetImportantRequest(subjectId);
        try {
            result = importantService.getImportantOfSubject(request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
