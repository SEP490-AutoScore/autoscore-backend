package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.DepartmentRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DepartmentUpdateRequestDTO {
    private Long id; // ID của phòng ban

    @NotBlank(message = "Department name must not be blank")
    private String departmentName;

    @NotBlank(message = "Development language must not be blank")
    private String devLanguage;
}
