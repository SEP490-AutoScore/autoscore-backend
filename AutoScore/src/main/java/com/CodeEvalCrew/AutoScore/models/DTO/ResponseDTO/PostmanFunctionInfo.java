package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PostmanFunctionInfo {
    private String functionName;
    private Long totalPmTest;

    // public PostmanFunctionInfo(String functionName, Long totalPmTest) {
    //     this.functionName = functionName;
    //     this.totalPmTest = totalPmTest;
    // }

    // public String getFunctionName() {
    //     return functionName;
    // }

    // public Long getTotalPmTest() {
    //     return totalPmTest;
    // }
}
