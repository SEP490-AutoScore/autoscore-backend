package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SubjectView {
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
}
