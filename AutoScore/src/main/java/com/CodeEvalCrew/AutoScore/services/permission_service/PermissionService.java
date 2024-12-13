package com.CodeEvalCrew.AutoScore.services.permission_service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.mappers.PermissionMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PermissionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionListResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionCategoryResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission_Category;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;
import com.CodeEvalCrew.AutoScore.repositories.permission_repository.IPermissionCategoryRepository;
import com.CodeEvalCrew.AutoScore.repositories.permission_repository.IPermissionRepository;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRolePermissionRepository;
import com.CodeEvalCrew.AutoScore.repositories.role_repository.IRoleRepository;
import com.CodeEvalCrew.AutoScore.services.role_service.IRolePermissionService;

@Service
public class PermissionService implements IPermissionService {

    private final IPermissionRepository permissionRepository;
    private final IPermissionCategoryRepository permissionCategoryRepository;
    private final IRolePermissionRepository rolePermissionRepository;
    private final IRoleRepository roleRepository;
    private final IRolePermissionService rolePermissionService;

    public PermissionService(IPermissionRepository permissionRepository, IPermissionCategoryRepository permissionCategoryRepository,
            IRolePermissionRepository rolePermissionRepository, IRolePermissionService rolePermissionService, IRoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.permissionCategoryRepository = permissionCategoryRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.rolePermissionService = rolePermissionService;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<PermissionResponseDTO> getAllPermissions() {
        try {
            List<Permission> permissions = permissionRepository.findAll();
            return permissions.stream().map(PermissionMapper.INSTANCE::permissionToPermissionResponseDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception("Error while getting all permissions");
        }
    }

    @Override
    public PermissionResponseDTO getPermissionById(Long permissionId) {
        try {
            Permission permission = permissionRepository.findById(permissionId).get();
            if (permission == null) {
                return null;
            }
            return PermissionMapper.INSTANCE.permissionToPermissionResponseDTO(permission);
        } catch (Exception e) {
            throw new Exception("Error while getting permission with id: " + permissionId);
        }
    }

    @Override
    public OperationStatus createPermission(PermissionRequestDTO permissionRequestDTO) {
        try {
            String permissionName = permissionRequestDTO.getPermissionName();
            String action = permissionRequestDTO.getAction().toUpperCase().trim();
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

            Optional<Permission_Category> permissionCategory = permissionCategoryRepository.findById(permissionCategoryId);
            if (permissionCategory.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            Permission permissionEntity = new Permission();
            permissionEntity.setPermissionName(permissionName);
            permissionEntity.setAction(action);
            permissionEntity.setDescription(permissionRequestDTO.getDescription());
            permissionEntity.setPermissionCategory(permissionCategory.get());
            permissionEntity.setStatus(true);

            Permission savedPermission = permissionRepository.save(permissionEntity);
            if (savedPermission == null || savedPermission.getPermissionId() == null) {
                return OperationStatus.FAILURE;
            }

            List<Role> roles = roleRepository.findAll();
            for (Role role : roles) {
                rolePermissionService.createRolePermission(savedPermission, role);
            }

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
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

            Optional<Permission_Category> permissionCategory = permissionCategoryRepository.findById(permissionCategoryId);
            if (permissionCategory.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            Permission permissionEntity = permissionRepository.findById(permissionId).get();
            permissionEntity.setPermissionName(permissionName);
            permissionEntity.setAction(action);
            permissionEntity.setPermissionCategory(permissionCategory.get());
            permissionEntity.setDescription(permissionRequestDTO.getDescription());

            Permission savedPermission = permissionRepository.save(permissionEntity);
            if (savedPermission == null || savedPermission.getPermissionId() == null) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    @Override
    public OperationStatus deletePermission(Long permissionId) {
        try {
            Optional<Permission> permission = permissionRepository.findById(permissionId);
            if (permission.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            List<Role_Permission> rolePermissions = getRolePermissionById(permissionId);
            if (rolePermissions.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            for (Role_Permission rolePermission : rolePermissions) {
                if (rolePermission.isStatus() && !rolePermission.getRole().getRoleCode().equals("ADMIN")) {
                    return OperationStatus.CANNOT_DELETE;
                }
            }

            for (Role_Permission rolePermission : rolePermissions) {
                rolePermissionRepository.delete(rolePermission);
            }

            permissionRepository.deleteById(permissionId);

            Optional<Permission> savedPermission = permissionRepository.findById(permissionId);
            if (savedPermission.isEmpty()) {
                return OperationStatus.SUCCESS;
            }

            return OperationStatus.FAILURE;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    private List<Role_Permission> getRolePermissionById(Long permissionId) {
        try {
            if (permissionId == null) {
                return null;
            }

            List<Role_Permission> rolePermissions = rolePermissionRepository.findAllByPermission_PermissionId(permissionId);
            if (rolePermissions == null || rolePermissions.isEmpty()) {
                throw new Exception("Role Permission not found with id: " + permissionId);
            }
            return rolePermissions;
        } catch (Exception e) {
            throw new Exception("Error while getting permission category with id: " + permissionId);
        }
    }

    @Override
    public List<PermissionCategoryResponseDTO> getAllPermissionByPermissionCategory() {
        try {
            List<Permission_Category> permissionCategories = permissionCategoryRepository.findAll()
                    .stream().filter(permissionCategory -> permissionCategory.isStatus()).collect(Collectors.toList());
            if (permissionCategories == null || permissionCategories.isEmpty()) {
                return null;
            }
            List<PermissionCategoryResponseDTO> permissionPermissionCategoryResponseDTOs = new ArrayList<>();
            for (Permission_Category permissionCategory : permissionCategories) {
                PermissionCategoryResponseDTO permissionPermissionCategoryResponseDTO = new PermissionCategoryResponseDTO();
                Optional<List<Permission>> permissions = permissionRepository.findAllByPermissionCategory(permissionCategory);
                if (permissions.isPresent() && !permissions.get().isEmpty()) {
                    permissionPermissionCategoryResponseDTO.setPermissionCategoryName(permissionCategory.getPermissionCategoryName());
                    permissionPermissionCategoryResponseDTO.setPermissionCategoryId(permissionCategory.getPermissionCategoryId());
                    permissionPermissionCategoryResponseDTO.setStatus(permissionCategory.isStatus());
                    permissionPermissionCategoryResponseDTO.setPermissions(new ArrayList<>());

                    List<Permission> permissionSorted = permissions.get()
                            .stream().sorted(Comparator.comparing(Permission::getPermissionName).reversed()).collect(Collectors.toList());

                    for (Permission permission : permissionSorted) {
                        if (permission.isStatus()) {
                            PermissionListResponseDTO permissionListResponseDTO = new PermissionListResponseDTO(
                                    permission.getPermissionId(),
                                    permission.getPermissionName(),
                                    permission.getDescription(),
                                    permission.getAction(),
                                    permission.isStatus());
                            permissionPermissionCategoryResponseDTO.getPermissions().add(permissionListResponseDTO);
                        }
                    }
                    permissionPermissionCategoryResponseDTOs.add(permissionPermissionCategoryResponseDTO);
                }
            }
            return permissionPermissionCategoryResponseDTOs
                    .stream().sorted(Comparator.comparing(PermissionCategoryResponseDTO::getPermissionCategoryName))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception("Error while getting all permissions");
        }
    }
}
