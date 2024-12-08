package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostmanForGradingGetDTO {
    private Long postmanForGradingId;
    private String postmanFunctionName;
    private Float scoreOfFunction;
    private Long totalPmTest;
    private boolean status;
    private Long orderPriority;
    private Long postmanForGradingParentId;
    private String fileCollectionPostman; // JSON string
    private Long examQuestionId;
    private Long gherkinScenarioId;
    private Long examPaperId;
    
}
