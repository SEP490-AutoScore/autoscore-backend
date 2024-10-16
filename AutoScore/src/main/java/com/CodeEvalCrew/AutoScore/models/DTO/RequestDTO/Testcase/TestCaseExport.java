package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Testcase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseExport {
    private String testCaseName;
    private double maxScore;
    private String testcaseBody;
    private String testcaseResponse;
}
