package com.CodeEvalCrew.AutoScore.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.EmployeeResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Employee;

@Mapper
public interface EmployeeMapper{
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    List<EmployeeResponseDTO> employeesToEmployeeResponseDTOs(List<Employee> employees);
}
