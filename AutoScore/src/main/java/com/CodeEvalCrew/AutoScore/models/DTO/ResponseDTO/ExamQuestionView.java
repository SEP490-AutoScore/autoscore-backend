package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamQuestionView {
    private Long examQuestionId;
    private String questionContent;
    private String questionNumber;
    private float maxScore;
    private String type;
}
