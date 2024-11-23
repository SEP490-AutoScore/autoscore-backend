package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamCreateRequestDTO {
    private String examCode;
    private LocalDateTime examAt;
    private LocalDateTime gradingAt;
    private LocalDateTime publishAt;
    private Long subjectId;
    private Long semesterId;
}
