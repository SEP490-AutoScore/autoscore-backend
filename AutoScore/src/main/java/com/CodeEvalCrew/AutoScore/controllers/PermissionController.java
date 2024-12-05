package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PermissionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionPermissionCategoryResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.services.permission_service.IPermissionService;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {
    private final IPermissionService permissionService;

    public PermissionController(IPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PreAuthorize("hasAnyAuthority('VIEW_PERMISSION', 'ALL_ACCESS')")
    @GetMapping
    public ResponseEntity<List<PermissionResponseDTO>> getAllPermissions() {
        List<PermissionResponseDTO> permissions = permissionService.getAllPermissions();
        if (permissions == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(permissions);
    }

    @PreAuthorize("hasAnyAuthority('VIEW_PERMISSION', 'ALL_ACCESS')")
    @GetMapping("/{permissionId}")
    public ResponseEntity<PermissionResponseDTO> getPermissionById(@PathVariable Long permissionId) {
        PermissionResponseDTO permission = permissionService.getPermissionById(permissionId);
        if (permission == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(permission);
    }

    @PreAuthorize("hasAnyAuthority('CREATE_PERMISSION', 'ALL_ACCESS')")
    @PostMapping("/create")
    public ResponseEntity<?> createPermission(@RequestBody PermissionRequestDTO permissionRequestDTO) {
        OperationStatus status = permissionService.createPermission(permissionRequestDTO);
        return switch (status) {
            case SUCCESS -> ResponseEntity.ok("Permission created successfully");
            case INVALID_INPUT -> ResponseEntity.badRequest().body("Invalid input data");
            case ALREADY_NAME_EXISTS -> ResponseEntity.status(409).body("Permission name already exists");
            case ALREADY_ACTION_EXISTS -> ResponseEntity.status(409).body("Permission action already exists");
            case FAILURE -> ResponseEntity.status(500).body("Can't create Permission");
            case NOT_FOUND -> ResponseEntity.status(404).body("Permission Category not found");
            case ERROR -> ResponseEntity.status(500).body("An error occurred while creating Permission");
            default -> ResponseEntity.status(500).body("Unexpected error occurred");
        };
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_PERMISSION', 'ALL_ACCESS')")
    @PostMapping("/update")
    public ResponseEntity<?> updatePermission(@RequestBody PermissionRequestDTO permissionRequestDTO) {
        OperationStatus status = permissionService.updatePermission(permissionRequestDTO);
        return switch (status) {
            case SUCCESS -> ResponseEntity.ok("Permission updated successfully");
            case INVALID_INPUT -> ResponseEntity.badRequest().body("Invalid input data");
            case ALREADY_NAME_EXISTS -> ResponseEntity.status(409).body("Permission name already exists");
            case ALREADY_ACTION_EXISTS -> ResponseEntity.status(409).body("Permission action already exists");
            case FAILURE -> ResponseEntity.status(500).body("Can't update Permission");
            case NOT_FOUND -> ResponseEntity.status(404).body("Permission not found");
            case ERROR -> ResponseEntity.status(500).body("An error occurred while updating Permission");
            default -> ResponseEntity.status(500).body("Unexpected error occurred");
        };
    }

    @PreAuthorize("hasAnyAuthority('DELETE_PERMISSION', 'ALL_ACCESS')")
    @PostMapping("/delete/{permissionId}")
    public ResponseEntity<?> deletePermission(@PathVariable Long permissionId) {
        OperationStatus status = permissionService.deletePermission(permissionId);
        return switch (status) {
            case SUCCESS -> ResponseEntity.ok("Permission deleted successfully");
            case FAILURE -> ResponseEntity.status(500).body("Can't delete Permission");
            case NOT_FOUND -> ResponseEntity.status(404).body("Permission not found");
            case CANNOT_DELETE -> ResponseEntity.status(400).body("Permission is in use");
            case ERROR -> ResponseEntity.status(500).body("An error occurred while deleting Permission");
            default -> ResponseEntity.status(500).body("Unexpected error occurred");
        };
    }

    @PreAuthorize("hasAnyAuthority('VIEW_PERMISSION', 'ALL_ACCESS')")
    @GetMapping("/get-all")
    public ResponseEntity<List<PermissionPermissionCategoryResponseDTO>> getAllPermissionByPermissionCategory() {
        List<PermissionPermissionCategoryResponseDTO> permissions = permissionService.getAllPermissionByPermissionCategory();
        if (permissions == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(permissions);
    }
}
