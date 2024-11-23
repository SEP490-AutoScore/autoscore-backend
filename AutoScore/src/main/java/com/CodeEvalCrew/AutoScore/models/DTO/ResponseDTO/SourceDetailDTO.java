package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SourceDetailDTO {
    private Long sourceDetailId;
    private String studentSourceCodePath;
    private Long studentId;
    // private Long sourceId;
}