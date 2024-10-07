package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import com.CodeEvalCrew.AutoScore.models.Entity.Permission_Category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDTO {
    private Long permissionId;
    private String permissionName;
    private String action;
    private boolean status;
    private Permission_Category permissionCategory;
}
    