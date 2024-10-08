package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequestDTO {
    @NotEmpty(message = "Email should not be empty")
    private String email;
    @NotEmpty(message = "Name should not be empty")
    private String name;
    @NotNull(message = "CampusId should not be empty")
    private Long campusId;
    @NotNull(message = "RoleId should not be empty")
    private Long roleId;
    private Long departmentId;
    private boolean isHeader;
}
