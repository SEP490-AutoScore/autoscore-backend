package com.CodeEvalCrew.AutoScore.services.account_service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AccountResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RoleResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.mappers.AccountMapper;
import com.CodeEvalCrew.AutoScore.mappers.RoleMapper;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRoleRepositoty;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class AccountService implements IAccountService {

    private final IAccountRepository accountRepository;
    private final IRoleRepositoty roleRepositoty;
    private final Util util;

    public AccountService(IAccountRepository accountRepository, IRoleRepositoty roleRepositoty, IEmployeeRepository employeeRepository) {
        this.accountRepository = accountRepository;
        this.roleRepositoty = roleRepositoty;
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

                Optional<Role> role = roleRepositoty.findById(account.getRole().getRoleId());
                RoleResponseDTO roleResponseDTO = RoleMapper.INSTANCE.roleToRoleResponseDTO(role.get(), util);
                accountResponseDTO.setRole(roleResponseDTO);

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
        throw new UnsupportedOperationException("Unimplemented method 'getAccountById'");
    }
}
