package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostmanForGradingDTO {
    private Long postmanForGradingId;
    private String postmanFunctionName;
    private Float scoreOfFunction;
    private Long totalPmTest;
    private Long orderBy;
    private Long postmanForGradingParentId; 
    private Long examQuestionId;           
    private Long gherkinScenarioId;        
}




