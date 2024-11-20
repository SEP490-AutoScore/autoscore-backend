package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

public class PostmanFunctionInfo {
    private String functionName;
    private Long totalPmTest;

    public PostmanFunctionInfo(String functionName, Long totalPmTest) {
        this.functionName = functionName;
        this.totalPmTest = totalPmTest;
    }

    public String getFunctionName() {
        return functionName;
    }

    public Long getTotalPmTest() {
        return totalPmTest;
    }
}
