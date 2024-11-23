package com.CodeEvalCrew.AutoScore.mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.RolePermissionResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Mapper
public interface RolePermissionMapper {
    RolePermissionMapper INSTANCE = Mappers.getMapper(RolePermissionMapper.class);

    // Ánh xạ Role_Permission -> RolePermissionResponseDTO
    @Mapping(source = "role.roleId", target = "roleId")
    @Mapping(source = "role.roleName", target = "roleName")
    @Mapping(source = "role.description", target = "description")
    @Mapping(source = "role.roleCode", target = "roleCode")
    @Mapping(source = "role.status", target = "status")
    RolePermissionResponseDTO rolePermissionToRolePermissionResponseDTO(Role_Permission rolePermission, @Context Util util);
    
    // Ánh xạ List<Role_Permission> -> List<RolePermissionResponseDTO>
    @Mapping(expression= "java(util.getEmployeeFullName(rolePermission.getCreatedBy()))", target = "createdBy")
    @Mapping(expression= "java(util.getEmployeeFullName(rolePermission.getUpdatedBy()))", target = "updatedBy")
    @Mapping(expression= "java(util.getEmployeeFullName(rolePermission.getDeletedBy()))", target = "deletedBy")
    List<RolePermissionResponseDTO> rolePermissionsToRolePermissionResponseDTOs(List<Role_Permission> rolePermissions, @Context Util util);

    // Ánh xạ Permission -> PermissionResponseDTO
    PermissionResponseDTO permissionToPermissionResponseDTO(Permission permission);

    // Chuyển Set<Role_Permission> thành Set<PermissionResponseDTO>
    default Set<PermissionResponseDTO> mapPermissions(Set<Role_Permission> rolePermissions) {
        return rolePermissions.stream()
            .map(rolePermission -> permissionToPermissionResponseDTO(rolePermission.getPermission()))
            .collect(Collectors.toSet());
    }

}
