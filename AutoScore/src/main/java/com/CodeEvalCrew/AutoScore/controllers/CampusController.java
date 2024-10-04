package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CampusRequest.CreateCampusRequest;
import com.CodeEvalCrew.AutoScore.models.Entity.Campus;
import com.CodeEvalCrew.AutoScore.services.campus_service.ICampusService;

@RestController
@RequestMapping("/api/campus")
public class CampusController {

    @Autowired
    private ICampusService campusService;

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') and hasAuthority('CREATE_CAMPUS')")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Campus createCampus(@RequestBody CreateCampusRequest request) {
        try {
            // Call service to create campus and return the created campus
            return campusService.createCampus(request);
        } catch (Exception e) {
            // Throw a runtime exception for now, you can handle it in a more detailed way
            // later
            throw new RuntimeException("Failed to create campus: " + e.getMessage());
        }
    }
}
