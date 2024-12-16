package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam;

import java.time.LocalDateTime;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PaginateEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamViewRequestDTO{
    private String searchString;
    private Long subjectId = null;
    private LocalDateTime examAt;
    private LocalDateTime gradingAt;
    private LocalDateTime publishAt;

    //paging
    PaginateEntity paginateEntity;
}