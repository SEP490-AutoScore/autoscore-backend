package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GherkinScenarioInfoDTO {
    private Long examPaperId;
    private Long examQuestionId;
    private Long gherkinScenarioId;
}