package com.CodeEvalCrew.AutoScore.services.account_service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.AccountRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AccountResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Employee;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.repositories.account_organization_repository.AccountOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.organization_repository.IOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.position_repository.IPositionRepository;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRoleRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class AccountService implements IAccountService {

    private final IAccountRepository accountRepository;
    private final IEmployeeRepository employeeRepository;
    private final IRoleRepository roleRepository;
    private final IPositionRepository positionRepository;
    private final IOrganizationRepository organizationRepository;
    private final AccountOrganizationRepository accountOrganizationRepository;

    public AccountService(IAccountRepository accountRepository, IEmployeeRepository employeeRepository,
            IRoleRepository roleRepository, IPositionRepository positionRepository, IOrganizationRepository organizationRepository,
            AccountOrganizationRepository accountOrganizationRepository) {
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.positionRepository = positionRepository;
        this.organizationRepository = organizationRepository;
        this.accountOrganizationRepository = accountOrganizationRepository;
    }

    @Override
    public List<AccountResponseDTO> getAllAccount() {
        try {
            Account acc = accountRepository.findById(Util.getAuthenticatedAccountId()).get();
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
            return null;
        }
    }

    @Override
    public AccountResponseDTO getAccountById(Long accountId) {
        try {
            Account account = accountRepository.findById(accountId).get();
            Employee employee = employeeRepository.findByAccount_AccountId(accountId);
            AccountResponseDTO accountResponseDTO = new AccountResponseDTO();
            accountResponseDTO.setAccountId(account.getAccountId());
            accountResponseDTO.setName(employee.getFullName());
            accountResponseDTO.setEmail(account.getEmail());
            accountResponseDTO.setRole(account.getRole().getRoleName());
            accountResponseDTO.setEmployeeCode(employee.getEmployeeCode());
            accountResponseDTO.setAvatar(account.getAvatar());
            accountResponseDTO.setPosition(employee.getPosition().getName());
            accountResponseDTO.setCampus(employee.getOrganization().getName());
            accountResponseDTO.setRoleId(account.getRole().getRoleId());
            accountResponseDTO.setCampusId(employee.getOrganization().getOrganizationId());
            accountResponseDTO.setPositionId(employee.getPosition().getPositionId());
            return accountResponseDTO;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public OperationStatus createAccount(AccountRequestDTO accountRequestDTO) {
        try {
            if (validateInput(accountRequestDTO, "create")) {
                List<AccountResponseDTO> accounts = getAllAccount();
                for (AccountResponseDTO account : accounts) {
                    if (accountRequestDTO.getEmail().equals(account.getEmail())) {
                        return OperationStatus.ALREADY_EXISTS;
                    }
                }
                Account account = new Account();
                Role role = roleRepository.findById(accountRequestDTO.getRoleId()).get();
                account.setEmail(accountRequestDTO.getEmail());
                account.setRole(role);
                account.setCreatedAt(Util.getCurrentDateTime());
                account.setCreatedBy(Util.getAuthenticatedAccountId());
                account.setPassword(generatePassword(12));
                account.setStatus(true);
                Account savedAccount = accountRepository.save(account);
                if (savedAccount == null) {
                    return OperationStatus.FAILURE;
                }
                Employee employee = new Employee();
                employee.setFullName(accountRequestDTO.getName());
                employee.setEmployeeCode(randomEmployeeCode(role.getRoleCode()));
                employee.setAccount(savedAccount);
                employee.setStatus(true);
                employee.setPosition(positionRepository.findById(accountRequestDTO.getPositionId()).get());
                employee.setOrganization(organizationRepository.findById(accountRequestDTO.getCampusId()).get());
                employeeRepository.save(employee);
                return OperationStatus.SUCCESS;
            }
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
        return OperationStatus.FAILURE;
    }

    @Override
    public OperationStatus updateAccount(AccountRequestDTO accountRequestDTO) {
        try {
            if (validateInput(accountRequestDTO, "update")) {
                List<AccountResponseDTO> accounts = getAllAccount();
                for (AccountResponseDTO account : accounts) {
                    if (!accountRequestDTO.getEmail().equals(account.getEmail())
                            || Objects.equals(accountRequestDTO.getAccountId(), account.getAccountId())) {
                    } else {
                        return OperationStatus.ALREADY_EXISTS;
                    }
                }
                Account account = accountRepository.findById(accountRequestDTO.getAccountId()).get();
                account.setEmail(accountRequestDTO.getEmail());
                account.setRole(roleRepository.findById(accountRequestDTO.getRoleId()).get());
                account.setUpdatedBy(Util.getAuthenticatedAccountId());
                accountRepository.save(account);

                Organization organization = organizationRepository.findById(accountRequestDTO.getCampusId()).get();
                Organization_Enum type = organization.getType();

                Employee employee = employeeRepository.findByAccount_AccountId(accountRequestDTO.getAccountId());
                employee.setFullName(accountRequestDTO.getName());
                employee.setPosition(positionRepository.findById(accountRequestDTO.getPositionId()).get());
                employee.setOrganization(organization);
                employeeRepository.save(employee);

                List<Account_Organization> accountOrganization = accountOrganizationRepository.findByAccount_AccountId(accountRequestDTO.getAccountId());
                for (Account_Organization accountOrganizationRoot : accountOrganization) {
                    if (accountOrganizationRoot.getOrganization().getType() == type) {
                        accountOrganizationRoot.setOrganization(organization);
                        accountOrganizationRepository.save(accountOrganizationRoot);
                    }
                }

                return OperationStatus.SUCCESS;
            }
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
        return OperationStatus.FAILURE;
    }

    @Override
    public OperationStatus deleteAccount(Long accountId) {
        try {
            Account account = accountRepository.findById(accountId).get();
            account.setDeletedBy(Util.getAuthenticatedAccountId());
            account.setDeletedAt(Util.getCurrentDateTime());
            account.setStatus(!account.isStatus());
            accountRepository.save(account);

            Employee employee = employeeRepository.findByAccount_AccountId(accountId);
            employee.setStatus(!employee.isStatus());
            employeeRepository.save(employee);
            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    @Override
    public OperationStatus updateProfile(AccountRequestDTO accountRequestDTO) {
        try {
            if (validateInput(accountRequestDTO, "updateProfile")) {
                return OperationStatus.FAILURE;
            }
            List<AccountResponseDTO> accounts = getAllAccount();
            Long accountId = Util.getAuthenticatedAccountId();
            for (AccountResponseDTO account : accounts) {
                if (accountRequestDTO.getEmail().equals(account.getEmail())
                        && !Objects.equals(accountId, account.getAccountId())) {
                    return OperationStatus.ALREADY_EXISTS;
                }
            }
            Account account = accountRepository.findById(accountId).orElseThrow();
            Employee employee = employeeRepository.findByAccount_AccountId(accountId);
            employee.setFullName(accountRequestDTO.getName());
            account.setUpdatedBy(accountId);
            if (accountRequestDTO.getAvatar() != null) {
                byte[] avatarBytes = Base64.getDecoder().decode(accountRequestDTO.getAvatar().split(",")[1]);
                account.setAvatar(avatarBytes);
            }
            if (accountRequestDTO.getOldPassword() != null) {
                if (accountRequestDTO.getNewPassword().equals(accountRequestDTO.getConfirmPassword())) {
                    account.setPassword(accountRequestDTO.getNewPassword());
                }
            }
            accountRepository.save(account);
            employeeRepository.save(employee);

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    private boolean validateInput(AccountRequestDTO accountRequestDTO, String type) {
        if (accountRequestDTO == null || type == null) {
            return false;
        }

        boolean hasRequiredFields
                = accountRequestDTO.getName() != null && !accountRequestDTO.getName().trim().isEmpty()
                && accountRequestDTO.getEmail() != null && !accountRequestDTO.getEmail().trim().isEmpty()
                && accountRequestDTO.getRoleId() != null
                && accountRequestDTO.getPositionId() != null
                && accountRequestDTO.getCampusId() != null;
        boolean hasRequiredFieldsForUpdate
                = accountRequestDTO.getName() != null && !accountRequestDTO.getName().trim().isEmpty()
                && accountRequestDTO.getEmail() != null && !accountRequestDTO.getEmail().trim().isEmpty();
        switch (type) {
            case "create" -> {
                return hasRequiredFields;
            }
            case "update" -> {
                return hasRequiredFields && accountRequestDTO.getAccountId() != null;
            }
            case "updateProfile" -> {
                return hasRequiredFieldsForUpdate && accountRequestDTO.getAccountId() != null;
            }
            default -> {
            }
        }
        return false;
    }

    private String randomEmployeeCode(String roleCode) {
        String prefix = roleCode.substring(0, 2).toUpperCase();
        long timestamp = Instant.now().toEpochMilli();
        String uniqueCode = String.valueOf(timestamp).substring(5);

        return prefix + uniqueCode;
    }

    private String generatePassword(int length) {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:,.<>?";
        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }
}
