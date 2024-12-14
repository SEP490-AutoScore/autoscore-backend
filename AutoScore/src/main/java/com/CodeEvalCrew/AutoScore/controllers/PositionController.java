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

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PositionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PositionResponseDTO;
import com.CodeEvalCrew.AutoScore.services.position_service.IPositionService;

@RestController
@RequestMapping("/api/position")
public class PositionController {

    private final IPositionService positionService;

    public PositionController(IPositionService positionService) {
        this.positionService = positionService;
    }

    @PreAuthorize("hasAnyAuthority('VIEW_POSITION', 'ALL_ACCESS')")
    @GetMapping
    public ResponseEntity<List<PositionResponseDTO>> getAllPosition() {
        try {
            List<PositionResponseDTO> positions = positionService.getAllPosition();
            if (positions.isEmpty()) {
                return ResponseEntity.status(404).build();
            }
            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_POSITION', 'ALL_ACCESS')")
    @GetMapping("/byRole")
    public ResponseEntity<List<PositionResponseDTO>> getAllPositionByRole() {
        try {
            List<PositionResponseDTO> positions = positionService.getAllPositionByRole();
            if (positions.isEmpty()) {
                return ResponseEntity.status(404).build();
            }
            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_POSITION', 'ALL_ACCESS')")
    @GetMapping("/{positionId}")
    public ResponseEntity<PositionResponseDTO> getPositionById(@PathVariable Long positionId) {
        try {
            PositionResponseDTO position = positionService.getPositionById(positionId);
            if (position == null) {
                return ResponseEntity.status(404).build();
            }
            return ResponseEntity.ok(position);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PreAuthorize("hasAnyAuthority('CREATE_POSITION', 'ALL_ACCESS')")
    @PostMapping("/create")
    public ResponseEntity<?> createPosition(@RequestBody PositionRequestDTO positionRequestDTO) {
        OperationStatus operationStatus = positionService.createPosition(positionRequestDTO);
        return switch (operationStatus) {
            case SUCCESS ->
                ResponseEntity.ok(operationStatus);
            case ALREADY_EXISTS ->
                ResponseEntity.status(409).body(operationStatus);
            case FAILURE ->
                ResponseEntity.status(500).body(operationStatus);
            case ERROR ->
                ResponseEntity.status(500).body(operationStatus);
            case INVALID_INPUT ->
                ResponseEntity.status(400).body(operationStatus);
            default ->
                ResponseEntity.status(500).body(operationStatus);
        };
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_POSITION', 'ALL_ACCESS')")
    @PostMapping("/update")
    public ResponseEntity<?> updatePosition(@RequestBody PositionRequestDTO positionRequestDTO) {
        OperationStatus operationStatus = positionService.updatePosition(positionRequestDTO);
        return switch (operationStatus) {
            case SUCCESS ->
                ResponseEntity.ok(operationStatus);
            case ALREADY_EXISTS ->
                ResponseEntity.status(409).body(operationStatus);
            case FAILURE ->
                ResponseEntity.status(500).body(operationStatus);
            case ERROR ->
                ResponseEntity.status(500).body(operationStatus);
            case INVALID_INPUT ->
                ResponseEntity.status(400).body(operationStatus);
            case NOT_FOUND ->
                ResponseEntity.status(404).body(operationStatus);
            default ->
                ResponseEntity.status(500).body(operationStatus);
        };
    }

    @PreAuthorize("hasAnyAuthority('DELETE_POSITION', 'ALL_ACCESS')")
    @PostMapping("/delete/{positionId}")
    public ResponseEntity<?> deletePosition(@PathVariable Long positionId) {
        OperationStatus operationStatus = positionService.deletePosition(positionId);
        return switch (operationStatus) {
            case SUCCESS ->
                ResponseEntity.ok(operationStatus);
            case FAILURE ->
                ResponseEntity.status(500).body(operationStatus);
            case ERROR ->
                ResponseEntity.status(500).body(operationStatus);
            case CANNOT_DELETE ->
                ResponseEntity.status(400).body(operationStatus);
            case NOT_FOUND ->
                ResponseEntity.status(404).body(operationStatus);
            default ->
                ResponseEntity.status(500).body(operationStatus);
        };
    }
}
