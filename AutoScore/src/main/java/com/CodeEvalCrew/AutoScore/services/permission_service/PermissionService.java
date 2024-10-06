package com.CodeEvalCrew.AutoScore.services.permission_service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PermissionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.repositories.permission_repository.IPermissionRepository;
import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.mappers.PermissionMapper;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission_Category;
import com.CodeEvalCrew.AutoScore.repositories.permission_repository.IPermissionCategoryRepository;

@Service
public class PermissionService implements IPermissionService {

    private final IPermissionRepository permissionRepository;
    private final IPermissionCategoryRepository permissionCatergoryRepository;

    public PermissionService(IPermissionRepository permissionRepository, IPermissionCategoryRepository permissionCatergoryRepository) {
        this.permissionRepository = permissionRepository;
        this.permissionCatergoryRepository = permissionCatergoryRepository;
    }

    @Override
    public List<PermissionResponseDTO> getAllPermissions() {
        try {
            List<Permission> permissions = permissionRepository.findAll();
            return permissions.stream().map(PermissionMapper.INSTANCE::permissionToPermissionResponseDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public PermissionResponseDTO getPermissionById(Long permissionId) {
        try {
            Permission permission = permissionRepository.findById(permissionId).get();
            if (permission == null) {
                throw new Exception("Permission not found with id: " + permissionId);
            }
            return PermissionMapper.INSTANCE.permissionToPermissionResponseDTO(permission);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public OperationStatus createPermission(PermissionRequestDTO permissionRequestDTO) {
        try {
            String permissionName = permissionRequestDTO.getPermissionName();
            String action = permissionRequestDTO.getAction();
            Long permissionCategoryId = permissionRequestDTO.getPermissionCategoryId();

            if (permissionName == null || action == null || permissionCategoryId == null) {
                return OperationStatus.INVALID_INPUT;
            }

            Optional<Permission> existingPermissionByName = permissionRepository.findByPermissionName(permissionName);
            if (existingPermissionByName.isPresent() && existingPermissionByName.get().isStatus()) {
                return OperationStatus.ALREADY_NAME_EXISTS;
            }

            Optional<Permission> existingPermissionByAction = permissionRepository.findByAction(action);
            if (existingPermissionByAction.isPresent() && existingPermissionByAction.get().isStatus()) {
                return OperationStatus.ALREADY_ACTION_EXISTS;
            }

            Optional<Permission_Category> permissionCategory = permissionCatergoryRepository.findById(permissionCategoryId);
            if (permissionCategory.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            Permission permissionEntity = new Permission();
            permissionEntity.setPermissionName(permissionName);
            permissionEntity.setAction(action);
            permissionEntity.setPermissionCategory(permissionCategory.get());
            permissionEntity.setStatus(true);

            Permission savedPermission = permissionRepository.save(permissionEntity);
            if (savedPermission == null || savedPermission.getPermissionId() == null) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public OperationStatus updatePermission(PermissionRequestDTO permissionRequestDTO) {
        try {
            Long permissionId = permissionRequestDTO.getPermissionId();
            String permissionName = permissionRequestDTO.getPermissionName();
            String action = permissionRequestDTO.getAction();
            Long permissionCategoryId = permissionRequestDTO.getPermissionCategoryId();

            if (permissionId == null || permissionName == null || action == null || permissionCategoryId == null) {
                return OperationStatus.INVALID_INPUT;
            }

            List<Permission> permissions = permissionRepository.findAll();

            for (Permission permission : permissions) {
                if (permission.getPermissionName().equals(permissionName)
                        && !Objects.equals(permission.getPermissionId(), permissionId)
                        && permission.isStatus()) {
                    return OperationStatus.ALREADY_NAME_EXISTS;
                }

                if (permission.getAction().equals(action)
                        && !Objects.equals(permission.getPermissionId(), permissionId)
                        && permission.isStatus()) {
                    return OperationStatus.ALREADY_ACTION_EXISTS;
                }
            }

            Optional<Permission_Category> permissionCategory = permissionCatergoryRepository.findById(permissionCategoryId);
            if (permissionCategory.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            Permission permissionEntity = permissionRepository.findById(permissionId).get();
            permissionEntity.setPermissionName(permissionName);
            permissionEntity.setAction(action);
            permissionEntity.setPermissionCategory(permissionCategory.get());

            Permission savedPermission = permissionRepository.save(permissionEntity);
            if (savedPermission == null || savedPermission.getPermissionId() == null) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public OperationStatus deletePermission(Long permissionId) {
        try {
            Permission permission = permissionRepository.findById(permissionId).get();
            if (permission == null) {
                return OperationStatus.NOT_FOUND;
            }

            permission.setStatus(false);
            Permission savedPermission = permissionRepository.save(permission);
            if (savedPermission == null || savedPermission.getPermissionId() == null) {
                return OperationStatus.FAILURE;
            }
            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}