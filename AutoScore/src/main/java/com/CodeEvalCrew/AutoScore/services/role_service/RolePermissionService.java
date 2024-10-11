package com.CodeEvalCrew.AutoScore.services.role_service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.RolePermissionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RolePermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRolePermissionRepository;
import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.mappers.RolePermissionMapper;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRoleRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.permission_repository.IPermissionRepository;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRoleRepositoty;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class RolePermissionService implements IRolePermissionService {

    private final IRolePermissionRepository rolePermissionRepository;
    private final IRoleRepositoty roleRepositoty;
    private final IPermissionRepository permissionRepository;
    private final IAccountRoleRepository accountRoleRepository;
    private final Util util;

    public RolePermissionService(IRolePermissionRepository rolePermissionRepository, IRoleRepositoty roleRepositoty,
            IPermissionRepository permissionRepository, IAccountRoleRepository accountRoleRepository,
            IAccountRepository accountRepository, IEmployeeRepository employeeRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleRepositoty = roleRepositoty;
        this.permissionRepository = permissionRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.util = new Util(employeeRepository);
    }

    @Override
    public RolePermissionResponseDTO getRolePermissionById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Id cannot be null");
            }

            List<Role_Permission> rolePermissions = rolePermissionRepository.findAllByRole_RoleId(id);

            if (rolePermissions == null || rolePermissions.isEmpty()) {
                return null;
            }

            // Lấy đối tượng Role đầu tiên (tất cả Role_Permission đều có cùng một Role)
            Role_Permission rolePermission = rolePermissions.get(0);

            // Chuyển các permission của Role sang Set<PermissionResponseDTO>
            Set<PermissionResponseDTO> permissions = RolePermissionMapper.INSTANCE.mapPermissions(rolePermissions.stream().collect(Collectors.toSet()));

            // Ánh xạ Role_Permission sang RolePermissionResponseDTO
            RolePermissionResponseDTO responseDTO = RolePermissionMapper.INSTANCE.rolePermissionToRolePermissionResponseDTO(rolePermission, util);
            responseDTO.setPermissions(permissions); // Set danh sách permission

            return responseDTO;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public OperationStatus createRolePermission(RolePermissionRequestDTO rolePermissionRequestDTO) {
        try {
            Long roleId = rolePermissionRequestDTO.getRoleId();
            List<Long> permissionIds = rolePermissionRequestDTO.getPermissionIds();
            if (roleId == null || permissionIds == null) {
                return OperationStatus.INVALID_INPUT;
            }

            Role role = getRoleById(roleId);
            if (role == null) {
                return OperationStatus.NOT_FOUND;
            }

            for (Long permissionId : permissionIds) {
                Role_Permission rolePermission = new Role_Permission();

                rolePermission.setRole(role);
                Permission permission = getPermissionById(permissionId);
                rolePermission.setPermission(permission);
                rolePermission.setStatus(true);
                rolePermission.setCreatedAt(LocalDateTime.now());
                rolePermission.setCreatedBy(Util.getAuthenticatedAccountId());

                Role_Permission savedRolePermission = rolePermissionRepository.save(rolePermission);
                if (savedRolePermission == null || savedRolePermission.getRolePermissionId() == null) {
                    return OperationStatus.FAILURE;
                }
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

    @Override
    @Transactional
    public OperationStatus deleteRolePermission(Long id) {
        try {
            if (id == null) {
                return OperationStatus.INVALID_INPUT;
            }
            List<Role_Permission> rolePermissions = rolePermissionRepository.findAllByRole_RoleId(id);
            if (rolePermissions == null || rolePermissions.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            List<Account_Role> accountRoles = accountRoleRepository.findAllByRole_RoleId(id);
            if (accountRoles != null && !accountRoles.isEmpty()) {
                return OperationStatus.CANNOT_DELETE;
            }

            for (Role_Permission rolePermission : rolePermissions) {
                rolePermissionRepository.delete(rolePermission);
            }
            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    private Role getRoleById(Long roleId) {
        try {
            if (roleId == null) {
                throw new Exception("Id cannot be null");
            }
            Role role = roleRepositoty.findById(roleId).get();
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
