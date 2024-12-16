package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import lombok.Data;

@Data
public class AccountRequestDTO {
    private Long accountId;
    private Long employeeId;
    private String name;
    private String email;
    private Long roleId;
    private Long positionId;
    private Long departmentId;
    private Long campusId;
    private String avatar;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
