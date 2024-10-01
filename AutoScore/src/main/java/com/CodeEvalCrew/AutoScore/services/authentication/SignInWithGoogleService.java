package com.CodeEvalCrew.AutoScore.services.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.AccountMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SignInWithGoogleResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;

@Service
public class SignInWithGoogleService implements ISingInWithGoogleService {
    private final IAccountRepository accountRepository;

    @Autowired
    public SignInWithGoogleService(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public SignInWithGoogleResponseDTO authenticateWithGoogle(String email) {
        Account account = accountRepository.findByEmail(email);
        if (account != null) {
            // Sử dụng mapper để chuyển đổi
            return AccountMapper.INSTANCE.accountToSignInWithGoogleResponseDTO(account);
        } else {
            throw new IllegalStateException("Account not found for email: " + email);
        }
    }

    
}
