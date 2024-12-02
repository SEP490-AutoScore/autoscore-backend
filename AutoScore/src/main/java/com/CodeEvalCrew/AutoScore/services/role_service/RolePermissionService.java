package com.CodeEvalCrew.AutoScore.services.role_service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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
        Long roleId = rolePermissionRequestDTO.getRoleId();
        List<Long> newPermissionIds = rolePermissionRequestDTO.getPermissionIds();

        if (roleId == null || newPermissionIds == null) {
            return OperationStatus.INVALID_INPUT;
        }

        Role role = getRoleById(roleId);
        if (role == null) {
            return OperationStatus.NOT_FOUND;
        }

        try {
            // Lấy danh sách các role_permission hiện tại
            List<Role_Permission> currentRolePermissions = rolePermissionRepository.findAllByRole_RoleId(roleId);
            Set<Long> currentPermissionIds = currentRolePermissions.stream()
                    .map(rp -> rp.getPermission().getPermissionId())
                    .collect(Collectors.toSet());

            // So sánh với danh sách mới
            Set<Long> newPermissionIdSet = new HashSet<>(newPermissionIds);

            // 1. Các quyền cần xóa (có trong current nhưng không có trong new)
            List<Role_Permission> permissionsToDeactivate = currentRolePermissions.stream()
                    .filter(rp -> !newPermissionIdSet.contains(rp.getPermission().getPermissionId()))
                    .collect(Collectors.toList());

            permissionsToDeactivate.forEach(rp -> {
                rp.setStatus(false); // Deactivate quyền
                rp.setUpdatedAt(LocalDateTime.now());
                rp.setUpdatedBy(Util.getAuthenticatedAccountId());
            });

            // 2. Các quyền cần thêm mới (có trong new nhưng không có trong current)
            List<Long> permissionsToAdd = newPermissionIds.stream()
                    .filter(permissionId -> !currentPermissionIds.contains(permissionId))
                    .collect(Collectors.toList());

            List<Role_Permission> newRolePermissions = permissionsToAdd.stream().map(permissionId -> {
                Role_Permission rolePermission = new Role_Permission();
                rolePermission.setRole(role);
                Permission permission = getPermissionById(permissionId);
                rolePermission.setPermission(permission);
                rolePermission.setStatus(true);
                rolePermission.setCreatedAt(LocalDateTime.now());
                rolePermission.setCreatedBy(Util.getAuthenticatedAccountId());
                rolePermission.setUpdatedAt(LocalDateTime.now());
                rolePermission.setUpdatedBy(Util.getAuthenticatedAccountId());
                return rolePermission;
            }).collect(Collectors.toList());

            // 3. Giữ nguyên các quyền không thay đổi (có trong cả current và new)
            List<Role_Permission> permissionsToKeep = currentRolePermissions.stream()
                    .filter(rp -> newPermissionIdSet.contains(rp.getPermission().getPermissionId()) && rp.isStatus() == false)
                    .collect(Collectors.toList());

            permissionsToKeep.forEach(rp -> {
                rp.setStatus(true); // Kích hoạt lại quyền
                rp.setUpdatedAt(LocalDateTime.now());
                rp.setUpdatedBy(Util.getAuthenticatedAccountId());
            });

            // Lưu tất cả thay đổi
            rolePermissionRepository.saveAll(permissionsToDeactivate);
            rolePermissionRepository.saveAll(newRolePermissions);
            rolePermissionRepository.saveAll(permissionsToKeep);

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
