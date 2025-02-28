package com.CodeEvalCrew.AutoScore.utils;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Employee;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.services.account_service.UserDetailsImpl;

@Component
public class Util {
    private final IEmployeeRepository employeeRepository;
    @Autowired
    private IAccountRepository accountRepository;

    public Util(IEmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
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

    public Account getAuthenticatedAccount() {
        try {
            Long accountId = getAuthenticatedAccountId();
            if (accountId != null) {
                return accountRepository.findById(accountId).get();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // Get the current date and time
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    // Get account name for mapping
    public String getEmployeeFullName(Long accountId) {
        if (accountId != null) {
            Employee employee = employeeRepository.findByAccount_AccountId(accountId);
            if (employee != null) {
                return employee.getFullName();
            }
        }
        return null;
    }

    // Get organizations
    public static Set<Organization> getOrganizations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getOrganizations();
        }
        return null;
    }   

    // Get campus
    public static String getCampus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getOrganizations().stream().filter(org -> org.getType() == Organization_Enum.CAMPUS).findFirst().get().getName();
        }
        return null;
    }
}
