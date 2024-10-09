package com.CodeEvalCrew.AutoScore.services.account_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AccountResponseDTO;

public interface IAccountService {
    List<AccountResponseDTO> getAllAccount();
    AccountResponseDTO getAccountById(Long accountId);
}
