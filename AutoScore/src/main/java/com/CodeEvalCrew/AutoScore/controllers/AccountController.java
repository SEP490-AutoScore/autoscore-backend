package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AccountResponseDTO;
import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.services.account_service.IAccountService;

@RestController
@RequestMapping("api/account/")
public class AccountController {
    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    @PreAuthorize("hasAnyAuthority('VIEW_ACCOUNT', 'ALL_ACCESS')")
    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        try {
            List<AccountResponseDTO> accounts = accountService.getAllAccount();
            if (accounts.isEmpty()) {
                return ResponseEntity.badRequest().build();
            } 
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    
}
