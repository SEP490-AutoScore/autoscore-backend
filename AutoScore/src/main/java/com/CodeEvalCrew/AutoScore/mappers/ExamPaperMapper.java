package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperView;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;

public interface ExamPaperMapper {
    ExamPaperMapper INSTANCE = Mappers.getMapper(ExamPaperMapper.class);

    Exam_Paper requestToExamPaper(ExamPaperCreateRequest request);
    ExamPaperView examPAperToView(Exam_Paper examPaper);
}
