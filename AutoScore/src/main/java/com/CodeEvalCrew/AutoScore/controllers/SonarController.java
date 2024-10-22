package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.utils.ThirdPartyUtil;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SonarQube.SonarQubeRunnerRequest;


@RestController
@RequestMapping("api/sonar/")
public class SonarController {
    @Autowired
    private final ThirdPartyUtil util;

    public SonarController(ThirdPartyUtil util) {
        this.util = util;
    }

    @PostMapping("")
    public ResponseEntity<?> postMethodName(@RequestBody SonarQubeRunnerRequest request) {
        //TODO: process POST request
        
        return new ResponseEntity<>("ServiceNot found",HttpStatus.NOT_FOUND);
    }
    

}
