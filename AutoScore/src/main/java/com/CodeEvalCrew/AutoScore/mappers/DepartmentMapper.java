package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.DepartmentResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Department;

@Mapper
public interface DepartmentMapper {
    DepartmentMapper INSTANCE = Mappers.getMapper(DepartmentMapper.class);

    @Mapping(source = "departmentId", target = "departmentId")
    @Mapping(source = "departmentName", target = "departmentName")
    @Mapping(source = "devLanguage", target = "devLanguage")
    @Mapping(source = "status", target = "status")
    DepartmentResponseDTO departmentToResponse(Department department);

    @Mapping(source = "departmentId", target = "departmentId")
    @Mapping(source = "departmentName", target = "departmentName")
    @Mapping(source = "devLanguage", target = "devLanguage")
    @Mapping(source = "status", target = "status")
    Department responseToDepartment(DepartmentResponseDTO dto);
}
