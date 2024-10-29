package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SonarQube.SonarQubeRunnerRequest;
import com.CodeEvalCrew.AutoScore.utils.ThirdPartyUtil;



@RestController
@RequestMapping("api/sonar/")
public class SonarController {
    @Autowired
    private final ThirdPartyUtil util;

    public SonarController(ThirdPartyUtil util) {
        this.util = util;
    }

    @PostMapping("")
    public ResponseEntity<?> createSonarProject(@RequestBody SonarQubeRunnerRequest request) {
        int result;
        try {
            result = util.sonarQubeRunner(request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }       
    }

    @GetMapping("")
    public ResponseEntity<?> getResultSonarProject() {
        String result;
        SonarQubeRunnerRequest request = new SonarQubeRunnerRequest();
        request.setHostURL("http://localhost:9000");
        request.setProjectKey("test");
        request.setToken("squ_8cc3080177d6bbb02dc68712db2299d08a1cda9b");
        try {
            result = util.sonarQubeResultFeatch(request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }   
    }
    
    

}
