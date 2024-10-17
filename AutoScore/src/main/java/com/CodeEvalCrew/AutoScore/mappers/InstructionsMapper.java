package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InstructionsMapper {
    InstructionsMapper INSTANCE = Mappers.getMapper(InstructionsMapper.class);
}
