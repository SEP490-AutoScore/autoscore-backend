package com.CodeEvalCrew.AutoScore.services.role_service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.RoleRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RoleResponseDTO;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRoleRepositoty;
import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.mappers.RoleMapper;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class RoleService implements IRoleService {

    private final IRoleRepositoty roleRepositoty;
    private final IRolePermissionService rolePermissionService;
    private final Util util;


    public RoleService(IRoleRepositoty roleRepositoty, IAccountRepository accountRepository, IRolePermissionService rolePermissionService) {
        this.roleRepositoty = roleRepositoty;
        this.rolePermissionService = rolePermissionService;
        this.util = new Util(accountRepository);
    }

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        try {
            List<Role> roles = roleRepositoty.findAll();
            return roles.stream()
                .map(role -> RoleMapper.INSTANCE.roleToRoleResponseDTO(role, util))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception("Error while getting all roles");
        }
    }

    @Override
    public RoleResponseDTO getRoleById(Long roleId) {
        try {
            Role role = roleRepositoty.findById(roleId).get();
            if (role == null) {
                return null;
            }

            return RoleMapper.INSTANCE.roleToRoleResponseDTO(role, util);
        } catch (Exception e) {
            throw new Exception("Error while getting role with id: " + roleId);
        }
    }

    @Override
    public OperationStatus createRole(RoleRequestDTO roleRequestDTO) {
        try {
            String roleName = roleRequestDTO.getRoleName().toUpperCase().trim();
            if (roleName == null || roleName.isEmpty()) {
                return OperationStatus.INVALID_INPUT;
            }

            Optional<Role> role = roleRepositoty.findByRoleName(roleName);
            if (role.isPresent() && role.get().isStatus()) {
                return OperationStatus.ALREADY_EXISTS;
            }

            Role roleEntity = new Role();
            roleEntity.setRoleName(roleName);
            roleEntity.setStatus(true);
            roleEntity.setCreatedAt(Util.getCurrentDateTime());
            roleEntity.setCreatedBy(Util.getAuthenticatedAccountId());

            Role savedRole = roleRepositoty.save(roleEntity);
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
            String roleName = roleRequestDTO.getRoleName().toUpperCase().trim();
            if (roleId == null || roleId <= 0 || roleName == null || roleName.isEmpty()) {
                return OperationStatus.INVALID_INPUT;
            }

            Optional<Role> role = roleRepositoty.findById(roleId);
            if (role.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            List<Role> roles = roleRepositoty.findAll();
            for (Role r : roles) {
                if (!Objects.equals(r.getRoleId(), roleId) && r.getRoleName().equals(roleName)) {
                    return OperationStatus.ALREADY_EXISTS;
                }
            }

            role.get().setRoleName(roleName);
            role.get().setUpdatedAt(Util.getCurrentDateTime());
            role.get().setUpdatedBy(Util.getAuthenticatedAccountId());
            Role savedRole = roleRepositoty.save(role.get());
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
            Optional<Role> role = roleRepositoty.findById(roleId);
            if (role.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            Set<Account_Role> account_roles = role.get().getAccount_roles();
            for (Account_Role account_role : account_roles) {
                if (Objects.equals(account_role.getAccountRoleId(), roleId)) {
                    return OperationStatus.CANNOT_DELETE;
                }
            }

            OperationStatus operationStatus = rolePermissionService.deleteRolePermission(roleId);
            if (operationStatus != OperationStatus.SUCCESS) {
                return OperationStatus.FAILURE;
            }

            roleRepositoty.deleteById(roleId);

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    @Override
    public RoleResponseDTO getRoleByName(String roleName) {
        try {
            Optional<Role> role = roleRepositoty.findByRoleName(roleName);
            if (role == null) {
                throw new Exception("Role not found with name: " + roleName);
            }

            return RoleMapper.INSTANCE.roleToRoleResponseDTO(role.get(), util);
        } catch (Exception e) {
            throw new Exception("Error while getting role with name: " + roleName);
        }
    }

}
