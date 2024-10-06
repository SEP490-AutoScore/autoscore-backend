package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission;

@Mapper
public interface PermissionMapper {
    PermissionMapper INSTANCE = Mappers.getMapper(PermissionMapper.class);

    @Mapping(source = "permissionId", target = "permissionId")
    @Mapping(source = "permissionName", target = "permissionName")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "permissionCategory", target = "permissionCategory")
    PermissionResponseDTO permissionToPermissionResponseDTO(Permission permission);
}
