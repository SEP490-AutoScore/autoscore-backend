package com.CodeEvalCrew.AutoScore.utils;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.services.account_service.UserDetailsImpl;

public class Util {
    private final IAccountRepository accountRepository;

    public Util(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Get the account id of the authenticated user
    public static Long getAuthenticatedAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getAccountId();
        }
        return null;
    }

    // Get the current date and time
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    // Get account name for mapping
    public String getAccountName(Long accountId) {
        // if (accountId != null) {
        //     Account account = accountRepository.findById(accountId).orElse(null);
        //     if (account != null) {
        //         return account.getName();
        //     }
        // }
        return null;
    }
}
