package com.CodeEvalCrew.AutoScore.services.account_service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AccountResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.models.Entity.Employee;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class AccountService implements IAccountService {

    private final IAccountRepository accountRepository;
    private final IEmployeeRepository employeeRepository;

    public AccountService(IAccountRepository accountRepository, IEmployeeRepository employeeRepository) {
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<AccountResponseDTO> getAllAccount() {
        try {            Account acc = accountRepository.findById(Util.getAuthenticatedAccountId()).get();
            Employee emp = employeeRepository.findByAccount_AccountId(acc.getAccountId());

            List<Employee> employeeByOrg = new ArrayList<>();
            if (emp.getOrganization().getType() == Organization_Enum.UNIVERSITY) {
                employeeByOrg = employeeRepository.findAll();
            } else if (emp.getOrganization().getType() == Organization_Enum.CAMPUS) {
                employeeByOrg = employeeRepository.findAllByOrganization_OrganizationId(emp.getOrganization().getOrganizationId());
            }

            List<Account> accountByRole = new ArrayList<>();
            if (null == acc.getRole().getRoleCode()) {
                List<Account> accounts = accountRepository.findAll();
                for (Account account : accounts) {
                    if (!account.getRole().getRoleCode().equals("ADMIN") && !account.getRole().getRoleCode().equals("EXAMINER")
                            && !account.getRole().getRoleCode().equals("HEAD_OF_DEPARTMENT")) {
                        accountByRole.add(account);
                    }
                }
            } else {
                switch (acc.getRole().getRoleCode()) {
                    case "ADMIN" ->
                        accountByRole = accountRepository.findAll();
                    case "EXAMINER" -> {
                        List<Account> accounts = accountRepository.findAll();
                        for (Account account : accounts) {
                            if (!account.getRole().getRoleCode().equals("ADMIN")) {
                                accountByRole.add(account);
                            }
                        }
                    }
                    case "HEAD_OF_DEPARTMENT" -> {
                        List<Account> accounts = accountRepository.findAll();
                        for (Account account : accounts) {
                            if (!account.getRole().getRoleCode().equals("ADMIN") && !account.getRole().getRoleCode().equals("EXAMINER")) {
                                accountByRole.add(account);
                            }
                        }
                    }
                    default -> {
                        List<Account> accounts = accountRepository.findAll();
                        for (Account account : accounts) {
                            if (!account.getRole().getRoleCode().equals("ADMIN") && !account.getRole().getRoleCode().equals("EXAMINER")
                                    && !account.getRole().getRoleCode().equals("HEAD_OF_DEPARTMENT")) {
                                accountByRole.add(account);
                            }
                        }
                    }
                }
            }

            List<Account> accountByOrg = new ArrayList<>();
            for (Employee employee : employeeByOrg) {
                accountByOrg.add(employee.getAccount());
            }

            List<Account> finalAccounts = new ArrayList<>();
            for (Account accountR : accountByRole) {
                for (Account accountO : accountByOrg) {
                    if (Objects.equals(accountR, accountO)) {
                        finalAccounts.add(accountR);
                    }
                }
            }
            if (finalAccounts.isEmpty()) {
                throw new RuntimeException("No records found");
            }

            List<AccountResponseDTO> accountResponseDTOs = new ArrayList<>();
            for (Account account : finalAccounts) {
                AccountResponseDTO accountResponseDTO = new AccountResponseDTO();
                Employee employee = employeeRepository.findByAccount_AccountId(account.getAccountId());
                accountResponseDTO.setAccountId(account.getAccountId());
                accountResponseDTO.setName(employee.getFullName());
                accountResponseDTO.setEmail(account.getEmail());
                accountResponseDTO.setRole(account.getRole().getRoleName());
                accountResponseDTO.setEmployeeCode(employee.getEmployeeCode());
                accountResponseDTO.setAvatar(account.getAvatar());
                accountResponseDTO.setStatus(account.isStatus() ? "Active" : "Inactive");
                accountResponseDTO.setPosition(employee.getPosition().getName());
                accountResponseDTO.setCampus(employee.getOrganization().getName());
                accountResponseDTO.setCreatedBy(account.getCreatedBy() == null ? null : employeeRepository.findByAccount_AccountId(account.getCreatedBy()).getFullName());
                accountResponseDTO.setUpdatedBy(account.getUpdatedBy() == null ? null : employeeRepository.findByAccount_AccountId(account.getUpdatedBy()).getFullName());
                accountResponseDTO.setDeletedBy(account.getDeletedBy() == null ? null : employeeRepository.findByAccount_AccountId(account.getDeletedBy()).getFullName());
                accountResponseDTO.setCreatedAt(account.getCreatedAt());
                accountResponseDTO.setUpdatedAt(account.getUpdatedAt());
                accountResponseDTO.setDeletedAt(account.getDeletedAt());
                accountResponseDTOs.add(accountResponseDTO);
            }

            return accountResponseDTOs.stream().sorted(Comparator.comparing(AccountResponseDTO::getCreatedAt).reversed()).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public AccountResponseDTO getAccountById(Long accountId) {
        throw new UnsupportedOperationException("Unimplemented method 'getAccountById'");
    }
}
