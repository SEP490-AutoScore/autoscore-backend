package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Grading;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GradingRequestForExam {
    private Long examPaperId;
    private String examType;
    private Long organizationId;
    private int numberDeploy;
    private Long memory_Megabyte;
    private Long processors;
}
