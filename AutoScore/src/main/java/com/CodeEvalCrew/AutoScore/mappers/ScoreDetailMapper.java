package com.CodeEvalCrew.AutoScore.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreDetailsResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Score_Detail;

@Mapper(componentModel = "spring")
public interface ScoreDetailMapper {

    @Mapping(source = "score.scoreId", target = "scoreId")
    @Mapping(source = "examQuestion.examQuestionId", target = "examQuestionId")
    ScoreDetailsResponseDTO scoreDetailEntityToDTO(Score_Detail scoreDetail);

    List<ScoreDetailsResponseDTO> scoreDetailEntitiesToDTOs(List<Score_Detail> scoreDetails);
}
