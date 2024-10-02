package com.CodeEvalCrew.AutoScore.services.exam_service;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam;

public interface IExamService{
    Exam getExamById(long id);
}
