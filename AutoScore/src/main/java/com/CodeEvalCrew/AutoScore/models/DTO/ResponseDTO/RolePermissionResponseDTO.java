package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionResponseDTO {
    private Long roleId;
    private String roleName;
    private String roleCode;
    private String description;
    private boolean status;
    private List<PermissionCategoryResponseDTO> permissionsCategory;
}
