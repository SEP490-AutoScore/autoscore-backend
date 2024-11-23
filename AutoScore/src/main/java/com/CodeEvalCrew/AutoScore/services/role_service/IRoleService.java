package com.CodeEvalCrew.AutoScore.services.role_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.RoleRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RoleResponseDTO;

public interface IRoleService {
    List<RoleResponseDTO> getAllRoles();
    RoleResponseDTO getRoleById(Long roleId);
    RoleResponseDTO getRoleByName(String roleName);
    OperationStatus createRole(RoleRequestDTO roleRequestDTO);
    OperationStatus updateRole(RoleRequestDTO roleRequestDTO);
    // OperationStatus deleteRole(Long roleId);
}
