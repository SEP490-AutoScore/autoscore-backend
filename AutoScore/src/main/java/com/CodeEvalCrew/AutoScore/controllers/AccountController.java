package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("api/account/")
public class AccountController {
    @GetMapping("testing/{param}")
    public String getMethodName(@PathVariable String param) {
        return param;
    }
    
}
