package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostmanForGradingGetbyIdDTO {
    private Long postmanForGradingId;
    private String postmanFunctionName;
    private Float scoreOfFunction;
    private Long totalPmTest;
    private boolean status;
    private Long orderPriority;
    private Long postmanForGradingParentId;
    private Long examQuestionId;
    private Long gherkinScenarioId;
    private Long examPaperId;
    private String fileCollectionPostman; 
    
}
