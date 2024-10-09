package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DepartmentResponseDTO {
    private Long departmentId;
    private String departmentName;
    private String devLanguage;
    private boolean status;

    @Override
    public String toString() {
        return "DepartmentResponseDTO{" +
                "departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                ", devLanguage='" + devLanguage + '\'' +
                ", status=" + status +
                '}';
    }
}
