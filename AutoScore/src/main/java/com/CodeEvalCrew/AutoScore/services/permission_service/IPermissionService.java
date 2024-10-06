package com.CodeEvalCrew.AutoScore.services.permission_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PermissionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionResponseDTO;

public interface IPermissionService {
    List<PermissionResponseDTO> getAllPermissions();
    PermissionResponseDTO getPermissionById(Long permissionId);
    OperationStatus createPermission(PermissionRequestDTO permissionRequestDTO);
    OperationStatus updatePermission(PermissionRequestDTO permissionRequestDTO);
    OperationStatus deletePermission(Long permissionId);
}
