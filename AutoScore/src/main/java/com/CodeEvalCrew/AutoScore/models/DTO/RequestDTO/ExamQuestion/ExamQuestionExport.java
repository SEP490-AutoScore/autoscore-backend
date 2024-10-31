package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem.ExamBaremExport;

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
    private List<ExamBaremExport> barems;
}
