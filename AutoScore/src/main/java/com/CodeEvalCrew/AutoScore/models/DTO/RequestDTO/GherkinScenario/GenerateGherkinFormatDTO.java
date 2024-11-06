package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.GherkinScenario;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GenerateGherkinFormatDTO {
    private List<Long> examQuestionIds;
}
