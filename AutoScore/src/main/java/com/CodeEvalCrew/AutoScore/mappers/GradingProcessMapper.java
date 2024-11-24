package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GradingProcessView;
import com.CodeEvalCrew.AutoScore.models.Entity.GradingProcess;

@Mapper
public interface GradingProcessMapper {
    GradingProcessMapper INSTANCE = Mappers.getMapper(GradingProcessMapper.class);

    GradingProcessView gradingProcessToView(GradingProcess gradingProcess);
}
