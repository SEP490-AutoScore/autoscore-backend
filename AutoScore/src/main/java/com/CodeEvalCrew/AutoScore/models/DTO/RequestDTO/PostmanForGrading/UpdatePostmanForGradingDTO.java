package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGrading;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class UpdatePostmanForGradingDTO {
    private Long postmanForGradingId;
    private Float scoreOfFunction;
    private Long orderBy;
    private Long postmanForGradingParentId;
    
}
