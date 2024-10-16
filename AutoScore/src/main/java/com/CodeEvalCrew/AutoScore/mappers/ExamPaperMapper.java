package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.factory.Mappers;

public interface ExamPaperMapper {
    ExamPaperMapper INSTANCE = Mappers.getMapper(ExamPaperMapper.class);
}
