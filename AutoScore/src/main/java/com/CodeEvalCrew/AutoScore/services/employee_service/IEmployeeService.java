package com.CodeEvalCrew.AutoScore.services.employee_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.EmployeeResponseDTO;

public interface IEmployeeService {
    List<EmployeeResponseDTO> getAllEmployees();
}
