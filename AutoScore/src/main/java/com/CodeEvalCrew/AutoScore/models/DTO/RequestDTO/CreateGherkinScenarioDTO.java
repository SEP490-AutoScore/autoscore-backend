package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGherkinScenarioDTO {
    private String gherkinData;
    private Long examQuestionId;
}