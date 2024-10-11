package com.CodeEvalCrew.AutoScore.services.account_service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AccountResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RoleResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.mappers.AccountMapper;
import com.CodeEvalCrew.AutoScore.mappers.RoleMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CreateAccountRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Role;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRoleRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRoleRepositoty;
import com.CodeEvalCrew.AutoScore.utils.Util;

import jakarta.transaction.Transactional;

@Service
public class AccountService implements IAccountService {

    private final IAccountRepository accountRepository;
    private final IAccountRoleRepository accountRoleRepository;
    private final Util util;

    public AccountService(IAccountRepository accountRepository, IAccountRoleRepository accountRoleRepository, 
                          IRoleRepositoty roleRepositoty, IEmployeeRepository employeeRepository) {
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.util = new Util(employeeRepository);
    }

    @Override
    public List<AccountResponseDTO> getAllAccount() {
        try {
            List<Account> accounts = accountRepository.findAll();
            if (accounts.isEmpty()) {
                throw new RuntimeException("No records found");
            }

            List<AccountResponseDTO> accountResponseDTOs = new ArrayList<>();
            for (Account account : accounts) {
                AccountResponseDTO accountResponseDTO = AccountMapper.INSTANCE
                        .accountToAccountResponseDTO(account, util);

                List<Role> roles = getRolesByAccountId(account.getAccountId());
                Set<RoleResponseDTO> roleResponseDTOs = roles.stream()
                        .map(role -> RoleMapper.INSTANCE.roleToRoleResponseDTO(role, util))
                        .collect(Collectors.toSet());
                accountResponseDTO.setRoles(roleResponseDTOs);

                accountResponseDTOs.add(accountResponseDTO);
            }

            // Trả về danh sách AccountResponseDTO
            return accountResponseDTOs;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public AccountResponseDTO getAccountById(Long accountId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccountById'");
    }

    private List<Role> getRolesByAccountId(Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        List<Account_Role> accountRoles = accountRoleRepository.findAllByAccount_AccountId(accountId);

        if (accountRoles == null || accountRoles.isEmpty()) {
            throw new RuntimeException("Account Role not found with id: " + accountId);
        }

        return accountRoles.stream()
                .map(Account_Role::getRole)
                .collect(Collectors.toList());
    }
}
