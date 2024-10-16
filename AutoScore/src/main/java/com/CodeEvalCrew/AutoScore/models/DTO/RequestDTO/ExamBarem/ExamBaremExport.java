package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Testcase.TestCaseExport;

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
    private double baremScore;
    private List<TestCaseExport> testCases;
}
