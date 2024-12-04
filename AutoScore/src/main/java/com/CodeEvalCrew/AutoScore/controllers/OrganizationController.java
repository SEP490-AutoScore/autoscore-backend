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

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.OrganizationRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OrganizationResponseDTO;
import com.CodeEvalCrew.AutoScore.services.organization_service.OrganizationService;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PreAuthorize("hasAnyAuthority('VIEW_ORGANIZATION', 'ALL_ACCESS')")
    @GetMapping
    public List<OrganizationResponseDTO> getAllOrganization() {
        return organizationService.getAllOrganization();
    }

    @PreAuthorize("hasAnyAuthority('VIEW_ORGANIZATION', 'ALL_ACCESS')")
    @GetMapping("/{organizationId}")
    public OrganizationResponseDTO getOrganization(@PathVariable Long organizationId) {
        return organizationService.getOrganization(organizationId);
    }

    @PreAuthorize("hasAnyAuthority('CREATE_ORGANIZATION', 'ALL_ACCESS')")
    @PostMapping("/create")
    public ResponseEntity<?> createOrganization(@RequestBody OrganizationRequestDTO organizationRequestDTO) {
        OperationStatus operationStatus = organizationService.createOrganization(organizationRequestDTO);
        return switch (operationStatus) {
            case SUCCESS -> ResponseEntity.ok(operationStatus);
            case ALREADY_EXISTS -> ResponseEntity.status(400).body(operationStatus);
            case FAILURE -> ResponseEntity.status(500).body(operationStatus);
            case ERROR -> ResponseEntity.status(500).body(operationStatus);
            default -> ResponseEntity.status(500).body(operationStatus);
        };
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_ORGANIZATION', 'ALL_ACCESS')")
    @PostMapping("/update")
    public ResponseEntity<?> updateOrganization(@RequestBody OrganizationRequestDTO organizationRequestDTO) {
        OperationStatus operationStatus = organizationService.updateOrganization(organizationRequestDTO);
        return switch (operationStatus) {
            case SUCCESS -> ResponseEntity.ok(operationStatus);
            case ALREADY_EXISTS -> ResponseEntity.status(400).body(operationStatus);
            case FAILURE -> ResponseEntity.status(500).body(operationStatus);
            case ERROR -> ResponseEntity.status(500).body(operationStatus);
            default -> ResponseEntity.status(500).body(operationStatus);
        };
    }

    @PreAuthorize("hasAnyAuthority('DELETE_ORGANIZATION', 'ALL_ACCESS')")
    @PostMapping("/delete/{organizationId}")
    public ResponseEntity<?> deleteOrganization(@PathVariable Long organizationId) {
        OperationStatus operationStatus = organizationService.deleteOrganization(organizationId);
        return switch (operationStatus) {
            case SUCCESS -> ResponseEntity.ok(operationStatus);
            case FAILURE -> ResponseEntity.status(500).body(operationStatus);
            case ERROR -> ResponseEntity.status(500).body(operationStatus);
            case CANNOT_DELETE -> ResponseEntity.status(400).body(operationStatus);
            case NOT_FOUND -> ResponseEntity.status(404).body(operationStatus);
            case ALREADY_EXISTS -> ResponseEntity.status(409).body(operationStatus);
            default -> ResponseEntity.status(500).body(operationStatus);
        };
    }
}
