package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamQuestionView;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;

public interface  ExamQuestionMapper {
    ExamQuestionMapper INSTANCE = Mappers.getMapper(ExamQuestionMapper.class);

    ExamQuestionView examQuestionToView(Exam_Question examQuestion);
    Exam_Question requestToExamQuestion(ExamQuestionCreateRequest request);
}
