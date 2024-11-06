package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamQuestionView {
    private Long examQuestionId;
    private String questionContent;
    private Float examQuestionScore;
    private String endPoint;
    private String roleAllow;
    private String httpMethod;
    private String description;
    private String payloadType;
    private String payload;
    private String validation;
    private String sucessResponse;
    private String errorResponse;
    private Long orderBy;
}
