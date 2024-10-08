package com.CodeEvalCrew.AutoScore.services.account_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CreateAccountRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AccountResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;

public interface IAccountService {
    List<AccountResponseDTO> getAllAccount();
    AccountResponseDTO getAccountById(Long accountId);
    OperationStatus createAccount(CreateAccountRequestDTO accountRequestDTO);
}
