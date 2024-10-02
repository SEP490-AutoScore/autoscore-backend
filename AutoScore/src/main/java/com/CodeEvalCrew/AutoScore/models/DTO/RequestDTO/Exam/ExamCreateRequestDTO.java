package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamCreateRequestDTO {
    private long examId;

    private String examCode;
    
    private Timestamp examAt;

    private Timestamp gradingAt;

    private Timestamp publishAt;

    private String semesterName;

    private long campusId;

    private long accountId;
    
    private long subjectId;
}
