package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class AccountController {
    @GetMapping("api/v1/get")
    public String getMethodName(@RequestParam String param) {
        return param;
    }
    
}
