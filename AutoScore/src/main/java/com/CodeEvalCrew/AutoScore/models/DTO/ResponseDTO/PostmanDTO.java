package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostmanDTO {
    private Long postmanForGradingId;
    private String postmanFunctionName;
    private Float scoreOfFunction;
    private Float scorePercentage;
    private Long totalPmTest;
    private boolean status;
    private Long orderBy;
    private Long postmanForGradingParentId;
    private String fileCollectionPostman;
    private Long examQuestionId;
    private Long gherkinScenarioId;
    private Long examPaperId;
}
