package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamPaperCreateRequest {
    private String examPaperCode;
    private Long examId;
    private Long examInstructionId = null;
    private Long examDatabaseId = null;
}
