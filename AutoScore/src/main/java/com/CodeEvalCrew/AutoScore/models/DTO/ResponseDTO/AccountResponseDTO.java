package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.time.LocalDateTime;
import java.util.Base64;

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
    private String avatar;
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

    public void setAvatar(byte[] avatarBytes) {
        if (avatarBytes != null) {
            this.avatar = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(avatarBytes);
        } else {
            this.avatar = null;
        }
    }
}
