package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamBaremCreate {
    private Long examBaremId = null;
    private String baremContent;
    private float baremMaxScore;
    private String baremURL;
    private Long examQuestionId;
}
