package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreOverViewResponseDTO {

    private Long examPaperId;
    private String examCode;
    private String examPaperCode;
    private String semesterName;
    private int totalStudents;
}
