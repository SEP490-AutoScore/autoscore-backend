package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.Role_PermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;

@Mapper
public interface  Role_PermissionMapper {
    Role_PermissionMapper INSTANCE = Mappers.getMapper(Role_PermissionMapper.class);

    @Mapping(source = "role.roleName", target = "roleName")
    @Mapping(source = "permission", target = "permissions")
    Role_PermissionResponseDTO rolePermissionToRolePermissionResponseDTO(Role_Permission rolePermission);
}
