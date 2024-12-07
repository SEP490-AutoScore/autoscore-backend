package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import lombok.Data;

@Data
public class AccountRequestDTO {
    private Long accountId;
    private Long employeeId;
    private String email;
    private String fullName;
    private Long roleId;
    private Long positionId;
    private Long departmentId;
    private Long campusId;
}
