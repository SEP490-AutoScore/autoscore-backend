package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.DepartmentRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DepartmentCreateRequestDTO {

    @NotBlank(message = "Department name must not be blank")
    private String departmentName;

    @NotBlank(message = "Development language must not be blank")
    private String devLanguage;
}
