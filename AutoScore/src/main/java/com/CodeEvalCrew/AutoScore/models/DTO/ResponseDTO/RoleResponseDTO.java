package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDTO {
    private Long roleId;
    private String roleName;
    private String roleCode;
    private String description;
    private boolean status;
    private LocalDateTime lastUpdatedAt;
    private String lastUpdatedBy;
    private int totalUser;
}
