package com.CodeEvalCrew.AutoScore.services.exam_service;

import com.CodeEvalCrew.AutoScore.models.DTO.ReponseEntity;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;

public interface IExamService{
    ReponseEntity<Exam> getExamById(long id) throws Exception;
}
