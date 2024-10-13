package com.CodeEvalCrew.AutoScore.mappers;

import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceDetailDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface SourceDetailMapper {

    @Mapping(source = "student.studentId", target = "studentId")
    @Mapping(source = "source.sourceId", target = "sourceId")
    SourceDetailDTO toDTO(Source_Detail sourceDetail);

    @Mapping(target = "student", ignore = true)  // Ignored as you will set it manually later
    @Mapping(target = "source", ignore = true)   // Ignored as you will set it manually later
    Source_Detail toEntity(SourceDetailDTO dto);
}