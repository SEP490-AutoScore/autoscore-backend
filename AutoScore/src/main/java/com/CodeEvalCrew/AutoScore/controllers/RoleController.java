package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.RoleRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RoleResponseDTO;
import com.CodeEvalCrew.AutoScore.services.role_service.IRoleService;

@RestController
@RequestMapping("/api/role")
public class RoleController {
    @Autowired
    private IRoleService roleService;

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('VIEW_ROLE')")
    @GetMapping("/getall")
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {
        try {
            List<RoleResponseDTO> roles = roleService.getAllRoles();
            if (roles == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('VIEW_ROLE')")
    @GetMapping("/getbyid")
    public ResponseEntity<RoleResponseDTO> getRoleById(Long roleId) {
        try {
            RoleResponseDTO role = roleService.getRoleById(roleId);
            if (role == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(role);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('VIEW_ROLE')")
    @PostMapping("/getbyname")
    public ResponseEntity<RoleResponseDTO> getRoleByName(String roleName) {
        try {
            RoleResponseDTO role = roleService.getRoleByName(roleName);
            if (role == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(role);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CREATE_ROLE')")
    @PostMapping("/create")
    public ResponseEntity<?> createRole(RoleRequestDTO roleRequestDTO) {
        try {
            OperationStatus status = roleService.createRole(roleRequestDTO);
            return switch (status) {
                case SUCCESS -> ResponseEntity.ok("Role created successfully");
                case INVALID_INPUT -> ResponseEntity.badRequest().body("Invalid input data");
                case ALREADY_EXISTS -> ResponseEntity.status(409).body("Role already exists");
                case FAILURE -> ResponseEntity.status(500).body("An error occurred while creating Role");
                case NOT_FOUND -> ResponseEntity.status(404).body("Role not found");
                default -> ResponseEntity.status(500).body("Unexpected error occurred");
            };
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error occurred");
        }
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('UPDATE_ROLE')")
    @PostMapping("/update")
    public ResponseEntity<?> updateRole(RoleRequestDTO roleRequestDTO) {
        try {
            OperationStatus status = roleService.updateRole(roleRequestDTO);
            return switch (status) {
                case SUCCESS -> ResponseEntity.ok("Role updated successfully");
                case INVALID_INPUT -> ResponseEntity.badRequest().body("Invalid input data");
                case ALREADY_EXISTS -> ResponseEntity.status(409).body("Role already exists");
                case FAILURE -> ResponseEntity.status(500).body("An error occurred while updating Role");
                case NOT_FOUND -> ResponseEntity.status(404).body("Role not found");
                default -> ResponseEntity.status(500).body("Unexpected error occurred");
            };
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error occurred");
        }
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('DELETE_ROLE')")
    @PostMapping("/delete")
    public ResponseEntity<?> deleteRole(Long roleId) {
        try {
            OperationStatus status = roleService.deleteRole(roleId);
            return switch (status) {
                case SUCCESS -> ResponseEntity.ok("Role deleted successfully");
                case FAILURE -> ResponseEntity.status(500).body("An error occurred while deleting Role");
                case NOT_FOUND -> ResponseEntity.status(404).body("Role not found");
                case CANNOT_DELETE -> ResponseEntity.status(400).body("Role is in use");
                default -> ResponseEntity.status(500).body("Unexpected error occurred");
            };
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error occurred");
        }
    }
}
