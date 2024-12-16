package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceView;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;

@Mapper
public interface SourceGradingMapper {
    SourceGradingMapper INSTANCE = Mappers.getMapper(SourceGradingMapper.class);

    SourceView sourceToView(Source source);
}
