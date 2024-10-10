package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountResponseDTO {
    private Long accountId;
    private String email;
    private String status;
    private String createdBy;
    private String updatedBy;
    private String deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Set<RoleResponseDTO> roles;
}
