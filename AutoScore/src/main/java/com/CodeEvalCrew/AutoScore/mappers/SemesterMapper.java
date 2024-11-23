package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.SemesterView;
import com.CodeEvalCrew.AutoScore.models.Entity.Semester;

@Mapper
public interface  SemesterMapper {
    SemesterMapper INSTANCE = Mappers.getMapper(SemesterMapper.class);

    SemesterView semesterToView(Semester semester);
}
