package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.AccountRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AccountResponseDTO;
import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
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

    @PreAuthorize("hasAnyAuthority('VIEW_PROFILE', 'ALL_ACCESS')")
    @GetMapping("profile/{id}")
    public ResponseEntity<AccountResponseDTO> getProfileById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(accountService.getAccountById(id));
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_PROFILE', 'ALL_ACCESS')")
    @PostMapping("profile/update")
    public ResponseEntity<OperationStatus> updateProfile(@RequestBody AccountRequestDTO account,
            @RequestBody MultipartFile file) {
        try {
            OperationStatus operationStatus = accountService.updateProfile(account, file);
            return switch (operationStatus) {
                case SUCCESS ->
                    ResponseEntity.ok(operationStatus);
                case ALREADY_EXISTS ->
                    ResponseEntity.status(409).body(operationStatus);
                case FAILURE ->
                    ResponseEntity.status(400).body(operationStatus);
                default ->
                    ResponseEntity.status(500).body(operationStatus);
            };
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('CREATE_ACCOUNT', 'ALL_ACCESS')")
    @PostMapping("create")
    public ResponseEntity<OperationStatus> createAccount(@RequestBody AccountRequestDTO account) {
        try {
            OperationStatus operationStatus = accountService.createAccount(account);
            return switch (operationStatus) {
                case SUCCESS ->
                    ResponseEntity.ok(operationStatus);
                case ALREADY_EXISTS ->
                    ResponseEntity.status(409).body(operationStatus);
                case FAILURE ->
                    ResponseEntity.status(400).body(operationStatus);
                default ->
                    ResponseEntity.status(500).body(operationStatus);
            };
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_ACCOUNT', 'ALL_ACCESS')")
    @PostMapping("update")
    public ResponseEntity<OperationStatus> updateAccount(@RequestBody AccountRequestDTO account) {
        try {
            OperationStatus operationStatus = accountService.updateAccount(account);
            return switch (operationStatus) {
                case SUCCESS ->
                    ResponseEntity.ok(operationStatus);
                case ALREADY_EXISTS ->
                    ResponseEntity.status(409).body(operationStatus);
                case FAILURE ->
                    ResponseEntity.status(400).body(operationStatus);
                default ->
                    ResponseEntity.status(500).body(operationStatus);
            };
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('DELETE_ACCOUNT', 'ALL_ACCESS')")
    @PostMapping("delete/{id}")
    public ResponseEntity<OperationStatus> deleteAccount(@PathVariable Long id) {
        try {
            OperationStatus operationStatus = accountService.deleteAccount(id);
            return switch (operationStatus) {
                case SUCCESS ->
                    ResponseEntity.ok(operationStatus);
                default ->
                    ResponseEntity.status(500).body(operationStatus);
            };
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
