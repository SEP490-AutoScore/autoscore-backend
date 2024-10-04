package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSubjectRequest {
    private String subjectName;
    private String subjectCode;
    private long departmentId;
    private long accountId;
}
