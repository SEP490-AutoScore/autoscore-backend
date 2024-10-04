package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CampusRequest.CreateCampusRequest;
import com.CodeEvalCrew.AutoScore.models.Entity.Campus;
import com.CodeEvalCrew.AutoScore.services.campus_service.ICampusService;

@RestController
@RequestMapping("/api/campus")
public class CampusController {

    @Autowired
    private ICampusService campusService;

    // @PostMapping("/create")
    // public ResponseEntity<ReponseEntity<Campus>> createCampus(@RequestBody CreateCampusRequest request) {
    //     ReponseEntity<Campus> response = new ReponseEntity<>();
    //     try {
    //         Campus campus = campusService.createCampus(request);
    //         response.ok(campus);
    //         return ResponseEntity.ok(response);
    //     } catch (Exception e) {
    //         response.error("Failed to create campus");
    //         return ResponseEntity.badRequest().body(response);
    //     }
    // }
}
