package com.CodeEvalCrew.AutoScore.services.employee_service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.EmployeeResponseDTO;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;
import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.mappers.EmployeeMapper;
import com.CodeEvalCrew.AutoScore.models.Entity.Employee;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;

@Service
public class EmployeeService implements IEmployeeService{
    private final IEmployeeRepository employeeRepository;

    public EmployeeService(IEmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        try {
            Set<Organization> organizations = Util.getOrganizations();
            if (organizations == null) {
                return null;
            }
            for (Organization organization : organizations) {
                if (organization.getType() == Organization_Enum.UNIVERSITY) {
                    List<Employee> employees = employeeRepository.findAll();
                    return EmployeeMapper.INSTANCE.employeesToEmployeeResponseDTOs(employees);
                }

                if (organization.getType() == Organization_Enum.CAMPUS) {
                    
                List<Employee> employees = employeeRepository.findByOrganization_OrganizationId(organization.getOrganizationId());
                if (employees != null) {
                    return EmployeeMapper.INSTANCE.employeesToEmployeeResponseDTOs(employees);
                }
                }
            }
            return null;
        } catch (Exception e) {
            throw new Exception("Error while getting all employees");
        }
    }
    
}
