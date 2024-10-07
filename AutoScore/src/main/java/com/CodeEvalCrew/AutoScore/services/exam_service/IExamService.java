package com.CodeEvalCrew.AutoScore.services.exam_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamViewRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;

public interface IExamService{
    ExamViewResponseDTO getExamById(long id)throws Exception,NotFoundException;

    List<ExamViewResponseDTO> GetExam(ExamViewRequestDTO request) throws Exception;

    ExamViewResponseDTO createNewExam(ExamCreateRequestDTO entity) throws Exception,NotFoundException;

    ExamViewResponseDTO updateExam (ExamCreateRequestDTO entity) throws Exception,NotFoundException;
}
