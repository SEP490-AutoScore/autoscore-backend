package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePermissionRequestDTO {
    private Long roleId;
    private Long permissionId;
    private boolean status;
}
