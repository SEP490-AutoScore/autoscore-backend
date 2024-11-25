package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper;

import java.util.List;

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
    private Long sunjectId;
    private String instruction;
    private int duration;
    private List<Long> importantIdList;//list important id
}
