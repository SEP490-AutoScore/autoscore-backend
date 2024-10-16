package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionCreateRequest {
    private String questionContent;
    private String questionNumber;
    private float maxScore;
    private String type;
    private Long examPaperId;
}