package com.CodeEvalCrew.AutoScore.services.role_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.RolePermissionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RolePermissionResponseDTO;

public interface IRolePermissionService {
    RolePermissionResponseDTO getRolePermissionById(Long id);
    OperationStatus createRolePermission(RolePermissionRequestDTO rolePermissionRequestDTO);
    OperationStatus updateRolePermission(RolePermissionRequestDTO rolePermissionRequestDTO);
    // OperationStatus deleteRolePermission(Long id);
}
