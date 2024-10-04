package com.CodeEvalCrew.AutoScore.services.permission_service;

import java.util.List;
import java.util.Optional;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PermissionCategoryRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionCategoryDTO;

public interface  IPermisionCategoryService {
    List<PermissionCategoryDTO> getAllPermissionCategory();
    Optional<PermissionCategoryDTO> getPermissionCategoryById(Long id);
    Optional<PermissionCategoryDTO> getPermissionCategoryByName(String name);
    OperationStatus createPermissionCategory(PermissionCategoryRequestDTO permissionCategoryDTO);
    OperationStatus updatePermissionCategory(PermissionCategoryRequestDTO permissionCategoryDTO);
    OperationStatus deletePermissionCategory(Long id);
}
