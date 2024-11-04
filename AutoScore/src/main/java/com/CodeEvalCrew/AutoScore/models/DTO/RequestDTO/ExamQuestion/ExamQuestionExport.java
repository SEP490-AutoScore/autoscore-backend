package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionExport {
    private String questionContent;
    private double questionScore;
}
