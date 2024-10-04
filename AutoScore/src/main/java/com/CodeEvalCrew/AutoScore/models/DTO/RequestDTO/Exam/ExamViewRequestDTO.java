package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam;

import java.sql.Timestamp;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PaginateEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamViewRequestDTO{
    private String searchString;

    private long campusId = 0;
    
    private long subjectId= 0;

    private Timestamp examAt;

    private Timestamp gradingAt;

    private Timestamp publishAt;

    private String semesterName;

    //paging
    PaginateEntity paginateEntity;
}