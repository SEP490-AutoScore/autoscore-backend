package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionRequestDTO {
    private Long permissionId;
    private String permissionName;
    private Long permissionCategoryId;
    private String action;
}
