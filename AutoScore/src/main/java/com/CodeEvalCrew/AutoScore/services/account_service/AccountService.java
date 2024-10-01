package com.CodeEvalCrew.AutoScore.services.account_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;

@Service
public class AccountService implements IAccountService {
    @Autowired
    private IAccountRepository accountRepository;

    // public String loadUser(String email) {
    //     Account account = accountRepository.findByEmail(email);

    // }
}
