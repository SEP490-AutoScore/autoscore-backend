package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostmanForGradingCreateDTO {
    private String postmanFunctionName;
    private Float scoreOfFunction;
    private String fileCollectionPostman; // JSON dưới dạng String
    private Long examQuestionId; // Có thể null
    private Long gherkinScenarioId; // Có thể null
    private Long examPaperId; // Không được null
}
