package com.CodeEvalCrew.AutoScore.services.role_service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.RolePermissionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RolePermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;

public interface IRolePermissionService {
    RolePermissionResponseDTO getRolePermissionById(Long id);
    OperationStatus createRolePermission(Permission permission, Role role);
    OperationStatus updateRolePermission(RolePermissionRequestDTO rolePermissionRequestDTO);
    // OperationStatus deleteRolePermission(Long id);
}
