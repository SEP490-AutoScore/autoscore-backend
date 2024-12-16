package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ExamPaperToExamRequest {
    private Long examPaperId;
    private Long examId; 
}
