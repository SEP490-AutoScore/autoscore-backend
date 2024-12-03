package com.CodeEvalCrew.AutoScore.services.role_service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.mappers.RoleMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.RoleRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RoleResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.permission_repository.IPermissionRepository;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRolePermissionRepository;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRoleRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

import jakarta.transaction.Transactional;

@Service
public class RoleService implements IRoleService {

    private final IRoleRepository roleRepository;
    private final IAccountRepository accountRepository;
    private final IEmployeeRepository employeeRepository;
    private final RolePermissionService rolePermissionService;
    private final IPermissionRepository permissionRepository;
    private final IRolePermissionRepository rolePermissionRepository;
    private final Util util;

    public RoleService(IRoleRepository roleRepository, IEmployeeRepository employeeRepository,
             RolePermissionService rolePermissionService, IAccountRepository accountRepository,
             IPermissionRepository permissionRepository, IRolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.rolePermissionService = rolePermissionService;
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.util = new Util(employeeRepository);
    }

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        try {
            List<RoleResponseDTO> roleResponseDTOs = new ArrayList<>();
            List<Role> roles = roleRepository.findAll();
            if (roles == null) {
                return null;
            }

            for (Role role : roles) {
                Optional<List<Account>> accounts = accountRepository.findAllByRoleRoleId(role.getRoleId());
                LocalDateTime lastUpdatedAt = role.getUpdatedAt() == null ? role.getCreatedAt() : role.getUpdatedAt();
                long lastUpdated = role.getUpdatedBy() == null ? role.getCreatedBy() : role.getUpdatedBy();
                String lastUpdatedBy = employeeRepository.findByAccount_AccountId(lastUpdated).getFullName();

                RoleResponseDTO roleResponse = new RoleResponseDTO();
                roleResponse.setRoleId(role.getRoleId());
                roleResponse.setRoleName(role.getRoleName());
                roleResponse.setRoleCode(role.getRoleCode());
                roleResponse.setStatus(role.isStatus());
                roleResponse.setDescription(role.getDescription());
                roleResponse.setLastUpdatedAt(lastUpdatedAt);
                roleResponse.setLastUpdatedBy(lastUpdatedBy);
                roleResponse.setTotalUser(accounts.isPresent() ? accounts.get().size() : 0);
                roleResponseDTOs.add(roleResponse);
            }

            return roleResponseDTOs;
        } catch (Exception e) {
            throw new Exception("Error while getting all roles");
        }
    }

    @Override
    public RoleResponseDTO getRoleById(Long roleId) {
        try {
            Role role = roleRepository.findById(roleId).get();
            if (role == null) {
                return null;
            }

            return RoleMapper.INSTANCE.roleToRoleResponseDTO(role, util);
        } catch (Exception e) {
            throw new Exception("Error while getting role with id: " + roleId);
        }
    }

    @Override
    @Transactional
    public OperationStatus createRole(RoleRequestDTO roleRequestDTO) {
        try {
            String roleName = roleRequestDTO.getRoleName().trim();
            String roleCode = roleRequestDTO.getRoleCode().toUpperCase().trim().replace(" ", "_");
            if (roleCode == null || roleCode.isEmpty()) {
                return OperationStatus.INVALID_INPUT;
            }

            Optional<Role> role = roleRepository.findByRoleName(roleName);
            if (role.isPresent() && role.get().isStatus()) {
                return OperationStatus.ALREADY_EXISTS;
            }
            Optional<Role> roleByCode = roleRepository.findByRoleCode(roleCode);
            if (roleByCode.isPresent() && roleByCode.get().isStatus()) {
                return OperationStatus.ALREADY_EXISTS;
            }

            Role roleEntity = new Role();
            roleEntity.setRoleCode(roleCode);
            roleEntity.setDescription(roleRequestDTO.getDescription());
            roleEntity.setRoleName(roleName);
            roleEntity.setStatus(true);
            roleEntity.setCreatedAt(Util.getCurrentDateTime());
            roleEntity.setCreatedBy(Util.getAuthenticatedAccountId());
            Role savedRole = roleRepository.save(roleEntity);

            List<Permission> permissions = permissionRepository.findAll();
            for (Permission permission : permissions) {
                rolePermissionService.createRolePermission(permission, savedRole);
            }

            if (savedRole == null || savedRole.getRoleId() == null) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    @Override
    public OperationStatus updateRole(RoleRequestDTO roleRequestDTO) {
        try {
            Long roleId = roleRequestDTO.getRoleId();
            String roleName = roleRequestDTO.getRoleName().trim();
            String roleCode = roleRequestDTO.getRoleCode().toUpperCase().trim().replace(" ", "_");
            if (roleId == null || roleId <= 0 || roleName == null || roleName.isEmpty() || roleCode == null || roleCode.isEmpty()) {
                return OperationStatus.INVALID_INPUT;
            }

            Optional<Role> role = roleRepository.findById(roleId);
            if (role.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            List<Role> roles = roleRepository.findAll();
            for (Role r : roles) {
                if (!Objects.equals(r.getRoleId(), roleId) && r.getRoleName().equals(roleName)) {
                    return OperationStatus.ALREADY_EXISTS;
                }

                if (!Objects.equals(r.getRoleId(), roleId) && r.getRoleCode().equals(roleCode)) {
                    return OperationStatus.ALREADY_EXISTS;
                }
            }

            role.get().setRoleName(roleName);
            role.get().setDescription(roleRequestDTO.getDescription());
            role.get().setRoleCode(roleCode);
            role.get().setUpdatedAt(Util.getCurrentDateTime());
            role.get().setUpdatedBy(Util.getAuthenticatedAccountId());
            Role savedRole = roleRepository.save(role.get());
            if (savedRole == null || savedRole.getRoleId() == null) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    @Override
    @Transactional
    public OperationStatus deleteRole(Long roleId) {
        try {
            Optional<Role> role = roleRepository.findById(roleId);
            if (role.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            Optional<List<Account>> accounts = accountRepository.findAllByRoleRoleId(roleId);
            if (accounts.isPresent() && !accounts.get().isEmpty()) {
                return OperationStatus.CANNOT_DELETE;
            }

            List<Role_Permission> rolePermissions = rolePermissionRepository.findAllByRole_RoleId(roleId);
            for (Role_Permission rolePermission : rolePermissions) {
                rolePermissionRepository.delete(rolePermission);
            }

            roleRepository.deleteById(roleId);
            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    @Override
    public RoleResponseDTO getRoleByName(String roleName) {
        try {
            Optional<Role> role = roleRepository.findByRoleName(roleName);
            if (role == null) {
                throw new Exception("Role not found with name: " + roleName);
            }

            return RoleMapper.INSTANCE.roleToRoleResponseDTO(role.get(), util);
        } catch (Exception e) {
            throw new Exception("Error while getting role with name: " + roleName);
        }
    }

}
