package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePermissionRequestDTO {
    private Long roleId;
    private List<Long> permissionIds;
}
