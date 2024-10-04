package com.CodeEvalCrew.AutoScore.services.exam_service;

import java.util.List;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamViewRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;

public interface IExamService{
    Exam getExamById(long id)throws Exception,NotFoundException;

    public List<ExamViewResponseDTO> GetExam(ExamViewRequestDTO request) throws Exception;

    ExamViewResponseDTO CreateNewExam(ExamCreateRequestDTO entity) throws Exception;
}
