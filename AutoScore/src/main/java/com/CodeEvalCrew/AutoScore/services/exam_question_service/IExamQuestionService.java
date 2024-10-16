package com.CodeEvalCrew.AutoScore.services.exam_question_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamQuestionView;

public interface IExamQuestionService {

    ExamQuestionView getById(Long id) throws NotFoundException;

    List<ExamQuestionView> getList(ExamQuestionViewRequest request) throws NotFoundException;

    ExamQuestionView createNewExamQuestion(ExamQuestionCreateRequest request) throws NotFoundException;

    ExamQuestionView updateExamQuestion(Long id, ExamQuestionCreateRequest request) throws NotFoundException;

    ExamQuestionView deleteExamQuestion(Long id) throws NotFoundException;

}
