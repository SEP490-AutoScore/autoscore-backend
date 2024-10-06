package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PermissionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.services.permission_service.IPermissionService;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {
    private final IPermissionService permissionService;

    public PermissionController(IPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('VIEW_PERMISSION')")
    @GetMapping("/getall")
    public List<PermissionResponseDTO> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('VIEW_PERMISSION')")
    @GetMapping("/getbyid/{permissionId}")
    public PermissionResponseDTO getPermissionById(@PathVariable Long permissionId) {
        return permissionService.getPermissionById(permissionId);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CREATE_PERMISSION')")
    @GetMapping("/create")
    public ResponseEntity<?> createPermission(PermissionRequestDTO permissionRequestDTO) {
        OperationStatus status = permissionService.createPermission(permissionRequestDTO);
        return switch (status) {
            case SUCCESS -> ResponseEntity.ok("Permission created successfully");
            case INVALID_INPUT -> ResponseEntity.badRequest().body("Invalid input data");
            case ALREADY_NAME_EXISTS -> ResponseEntity.status(409).body("Permission name already exists");
            case ALREADY_ACTION_EXISTS -> ResponseEntity.status(409).body("Permission action already exists");
            case FAILURE -> ResponseEntity.status(500).body("An error occurred while creating Permission");
            case NOT_FOUND -> ResponseEntity.status(404).body("Permission Category not found");
            default -> ResponseEntity.status(500).body("Unexpected error occurred");
        };
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('UPDATE_PERMISSION')")
    @GetMapping("/update")
    public ResponseEntity<?> updatePermission(PermissionRequestDTO permissionRequestDTO) {
        OperationStatus status = permissionService.updatePermission(permissionRequestDTO);
        return switch (status) {
            case SUCCESS -> ResponseEntity.ok("Permission updated successfully");
            case INVALID_INPUT -> ResponseEntity.badRequest().body("Invalid input data");
            case ALREADY_NAME_EXISTS -> ResponseEntity.status(409).body("Permission name already exists");
            case ALREADY_ACTION_EXISTS -> ResponseEntity.status(409).body("Permission action already exists");
            case FAILURE -> ResponseEntity.status(500).body("An error occurred while updating Permission");
            case NOT_FOUND -> ResponseEntity.status(404).body("Permission not found");
            default -> ResponseEntity.status(500).body("Unexpected error occurred");
        };
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('DELETE_PERMISSION')")
    @GetMapping("/delete/{permissionId}")
    public ResponseEntity<?> deletePermission(@PathVariable Long permissionId) {
        OperationStatus status = permissionService.deletePermission(permissionId);
        return switch (status) {
            case SUCCESS -> ResponseEntity.ok("Permission deleted successfully");
            case FAILURE -> ResponseEntity.status(500).body("An error occurred while deleting Permission");
            case NOT_FOUND -> ResponseEntity.status(404).body("Permission not found");
            default -> ResponseEntity.status(500).body("Unexpected error occurred");
        };
    }
}
