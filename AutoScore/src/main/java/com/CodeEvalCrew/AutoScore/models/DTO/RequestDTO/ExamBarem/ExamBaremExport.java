package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamBaremExport {
    private String baremContent;
    private float baremMaxScore;
    private String endpoint;
    private String allowRole;
    private String method;
    private String baremFunction;
    private String payloadType;
    private String payload;
    private String validation;
    private String successResponse;
    private String errorResponse;
    // private List<TestCaseExport> testCases;
}
