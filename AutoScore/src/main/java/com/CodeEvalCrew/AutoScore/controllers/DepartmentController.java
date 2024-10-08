package com.CodeEvalCrew.AutoScore.controllers;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.DepartmentRequest.DepartmentCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.DepartmentRequest.DepartmentUpdateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.DepartmentResponseDTO;
import com.CodeEvalCrew.AutoScore.services.department_service.IDepartmentService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    private final IDepartmentService departmentService;

    @Autowired
    public DepartmentController(IDepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') and hasAuthority('VIEW_DEPARTMENT')")
    @GetMapping
    public ResponseEntity<Page<DepartmentResponseDTO>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DepartmentResponseDTO> departments = departmentService.getAllDepartments(pageable);
        return ResponseEntity.ok(departments);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') and hasAuthority('VIEW_DEPARTMENT')")
    @GetMapping("/{devLanguage}")
    public ResponseEntity<Page<DepartmentResponseDTO>> getDepartmentsByDevLanguage(
            @PathVariable String devLanguage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DepartmentResponseDTO> departments = departmentService.getDepartmentsByDevLanguage(devLanguage, pageable);
        return ResponseEntity.ok(departments);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') and hasAuthority('CREATE_DEPARTMENT')")
    @PostMapping
    public ResponseEntity<Object> createDepartment(@RequestBody @Valid DepartmentCreateRequestDTO request) {
        return departmentService.createDepartment(request);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') and hasAuthority('UPDATE_DEPARTMENT')")
    @PutMapping
    public ResponseEntity<Object> updateDepartment(@RequestBody @Valid DepartmentUpdateRequestDTO request) {
        return departmentService.updateDepartment(request);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') and hasAuthority('DELETE_DEPARTMENT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteDepartment(@PathVariable Long id) {
        return departmentService.deleteDepartment(id);
    }

}
