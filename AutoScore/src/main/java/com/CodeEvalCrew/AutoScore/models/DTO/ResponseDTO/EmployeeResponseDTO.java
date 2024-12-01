package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResponseDTO {
    private Long employeeId;
    private String fullName;
    private String employeeCode;
    private PositionResponseDTO position;
    private OrganizationResponseDTO organization;
    private AccountResponseDTOReview account;
}
