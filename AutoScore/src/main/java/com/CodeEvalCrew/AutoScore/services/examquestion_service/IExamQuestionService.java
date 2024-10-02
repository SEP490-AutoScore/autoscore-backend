package com.CodeEvalCrew.AutoScore.services.examquestion_service;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;

import java.util.List;

public interface IExamQuestionService {
    List<Exam_Question> getExamQuestionsByExamPaperId(long examPaperId);
}
