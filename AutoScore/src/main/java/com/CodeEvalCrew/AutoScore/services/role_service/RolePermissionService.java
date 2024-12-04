package com.CodeEvalCrew.AutoScore.services.role_service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.RolePermissionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionListResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionPermissionCategoryResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RolePermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.permission_repository.IPermissionRepository;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRolePermissionRepository;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRoleRepository;
import com.CodeEvalCrew.AutoScore.services.permission_service.PermissionService;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class RolePermissionService implements IRolePermissionService {

    private final IRolePermissionRepository rolePermissionRepository;
    private final IRoleRepository roleRepository;
    private final IPermissionRepository permissionRepository;
    private final PermissionService permissionService;

    public RolePermissionService(IRolePermissionRepository rolePermissionRepository, IRoleRepository roleRepository,
            IPermissionRepository permissionRepository, IAccountRepository accountRepository, IEmployeeRepository employeeRepository,
            @Lazy PermissionService permissionService) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionService = permissionService;
    }

    @Override
    public RolePermissionResponseDTO getRolePermissionById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Id cannot be null");
            }
            Role role = roleRepository.findById(id).get();
            List<Role_Permission> rolePermissions = rolePermissionRepository.findAllByRole_RoleId(id);
            if (rolePermissions == null || rolePermissions.isEmpty()) {
                return null;
            }

            List<PermissionPermissionCategoryResponseDTO> permissionCategories = permissionService.getAllPermissionByPermissionCategory();
            if (permissionCategories == null || permissionCategories.isEmpty()) {
                return null;
            }

            for (Role_Permission rolePermission : rolePermissions) {
                for (PermissionPermissionCategoryResponseDTO permissionCategory : permissionCategories) {
                    for (PermissionListResponseDTO permission : permissionCategory.getPermissions()) {
                        if (rolePermission.getPermission().getPermissionId().equals(permission.getPermissionId())) {
                            permission.setStatus(rolePermission.isStatus());
                        }
                    }
                }
            }

            RolePermissionResponseDTO rolePermissionResponseDTO = new RolePermissionResponseDTO();
            rolePermissionResponseDTO.setRoleId(role.getRoleId());
            rolePermissionResponseDTO.setRoleName(role.getRoleName());
            rolePermissionResponseDTO.setRoleCode(role.getRoleCode());
            rolePermissionResponseDTO.setDescription(role.getDescription());
            rolePermissionResponseDTO.setStatus(role.isStatus());
            rolePermissionResponseDTO.setPermissionsCategory(permissionCategories);
            return rolePermissionResponseDTO;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public OperationStatus createRolePermission(Permission permission, Role role) {
        try {
            Role_Permission rolePermission = new Role_Permission();

            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermission.setStatus(false);
            rolePermission.setCreatedAt(LocalDateTime.now());
            rolePermission.setCreatedBy(Util.getAuthenticatedAccountId());

            Role_Permission savedRolePermission = rolePermissionRepository.save(rolePermission);
            if (savedRolePermission == null || savedRolePermission.getRolePermissionId() == null) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    @Override
    @Transactional
    public OperationStatus updateRolePermission(RolePermissionRequestDTO rolePermissionRequestDTO) {
        try {
            Optional<Role> role = roleRepository.findById(rolePermissionRequestDTO.getRoleId());
            if (role.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }
            Optional<Permission> permission = permissionRepository.findById(rolePermissionRequestDTO.getPermissionId());
            if (permission.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }
            Optional<Role_Permission> rolePermission
                    = rolePermissionRepository.findByRole_RoleIdAndPermission_PermissionId(
                            rolePermissionRequestDTO.getRoleId(), rolePermissionRequestDTO.getPermissionId());
            if (rolePermission.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }
            rolePermission.get().setStatus(rolePermissionRequestDTO.isStatus());
            rolePermissionRepository.save(rolePermission.get());
            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    // @Override
    // @Transactional
    // public OperationStatus deleteRolePermission(Long id) {
    //     try {
    //         if (id == null) {
    //             return OperationStatus.INVALID_INPUT;
    //         }
    //         List<Role_Permission> rolePermissions = rolePermissionRepository.findAllByRole_RoleId(id);
    //         if (rolePermissions == null || rolePermissions.isEmpty()) {
    //             return OperationStatus.NOT_FOUND;
    //         }
    //         List<Account_Role> accountRoles = accountRoleRepository.findAllByRole_RoleId(id);
    //         if (accountRoles != null && !accountRoles.isEmpty()) {
    //             return OperationStatus.CANNOT_DELETE;
    //         }
    //         for (Role_Permission rolePermission : rolePermissions) {
    //             rolePermissionRepository.delete(rolePermission);
    //         }
    //         return OperationStatus.SUCCESS;
    //     } catch (Exception e) {
    //         return OperationStatus.ERROR;
    //     }
    // }
    private Role getRoleById(Long roleId) {
        try {
            if (roleId == null) {
                throw new Exception("Id cannot be null");
            }
            Role role = roleRepository.findById(roleId).get();
            if (role == null) {
                throw new Exception("Role not found with id: " + roleId);
            }
            return role;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private Permission getPermissionById(Long permissionId) {
        try {
            if (permissionId == null) {
                throw new Exception("Id cannot be null");
            }
            Permission permission = permissionRepository.findById(permissionId).get();
            if (permission == null) {
                throw new Exception("Permission not found with id: " + permissionId);
            }
            return permission;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
