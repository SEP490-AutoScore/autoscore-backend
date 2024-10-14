package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RoleResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Mapper
public interface RoleMapper {
    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class); 

    @Mapping(expression= "java(util.getEmployeeFullName(role.getCreatedBy()))", target = "createdBy")
    @Mapping(expression= "java(util.getEmployeeFullName(role.getUpdatedBy()))", target = "updatedBy")
    @Mapping(expression= "java(util.getEmployeeFullName(role.getDeletedBy()))", target = "deletedBy")
    RoleResponseDTO roleToRoleResponseDTO(Role role, @Context Util util);

}
