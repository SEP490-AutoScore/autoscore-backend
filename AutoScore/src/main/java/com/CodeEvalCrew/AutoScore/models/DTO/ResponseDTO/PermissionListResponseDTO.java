package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionListResponseDTO {
    private Long permissionId;
    private String permissionName;
    private String description;
    private String action;
    private boolean status;
}
