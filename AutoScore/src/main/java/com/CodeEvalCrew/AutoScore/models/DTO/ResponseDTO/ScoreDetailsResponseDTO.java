package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.Data;

@Data
public class ScoreDetailsResponseDTO {

    private Long scoreDetailId;
    private String postmanFunctionName;
    private Float scoreOfFunction;
    private Long totalPmtest;
    private Float scoreAchieve;
    private Long noPmtestAchieve;
    private Long scoreId;
    private Long examQuestionId;
}
