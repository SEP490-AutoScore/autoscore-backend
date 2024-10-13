package com.CodeEvalCrew.AutoScore.mappers;

import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceDTO;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SourceMapper {

    SourceDTO toDTO(Source source);

    Source toEntity(SourceDTO dto);
}
