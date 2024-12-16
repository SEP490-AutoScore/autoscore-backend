package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ExamWithPapersDTO {
    private String examCode;
    private String examPaperCode;
    private Long examPaperId;
}