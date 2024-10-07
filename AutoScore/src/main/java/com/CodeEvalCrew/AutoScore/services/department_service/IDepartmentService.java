package com.CodeEvalCrew.AutoScore.services.department_service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.DepartmentRequest.DepartmentCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.DepartmentRequest.DepartmentUpdateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.DepartmentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;


public interface IDepartmentService {



    Page<DepartmentResponseDTO> getAllDepartments(Pageable pageable);
    Page<DepartmentResponseDTO> getDepartmentsByDevLanguage(String devLanguage, Pageable pageable);
ResponseEntity<Object> createDepartment(DepartmentCreateRequestDTO request);
  ResponseEntity<Object> updateDepartment(DepartmentUpdateRequestDTO request); 
  ResponseEntity<Object> deleteDepartment(Long id); 
}
