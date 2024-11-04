package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamPaperView {
    private Long examPaperId;
    private String examPaperCode;
    private String instruction;
    // private List<ImportantView> importants;
}
