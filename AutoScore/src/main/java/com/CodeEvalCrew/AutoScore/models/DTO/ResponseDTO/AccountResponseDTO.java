package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountResponseDTO {
    private Long accountId;
    private String name;
    private String email;
    private String role;
    private String employeeCode;
    private byte[] avatar;
    private String status;
    private String position;
    private String campus;
    private String createdBy;
    private String updatedBy;
    private String deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Long roleId;
    private Long campusId;
    private Long positionId;
}
