package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIPromptView;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Prompt;

@Mapper
public interface AIPromptMapper{
    AIPromptMapper INSTANCE = Mappers.getMapper(AIPromptMapper.class);
    @Mapping(source = "aiPromptId", target = "aiPromptId")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "languageCode", target = "languageCode")
    @Mapping(source = "for_ai", target = "for_ai")
    @Mapping(source = "type", target = "type")

    AIPromptView entityToPromptView(AI_Prompt entity);
    
}