package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GherkinScenarioDTO {
    private Long gherkinScenarioId;
    private String gherkinData;
    private boolean status;
    private Long examQuestionId;
    private Long postmanForGradingId;
}