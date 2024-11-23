package com.CodeEvalCrew.AutoScore.services.exam_service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.docx4j.model.fields.merge.DataFieldName;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamViewRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;

public interface IExamService{
    ExamViewResponseDTO getExamById(long id)throws Exception,NotFoundException;

    List<ExamViewResponseDTO> GetExam(ExamViewRequestDTO request) throws Exception;

    ExamViewResponseDTO createNewExam(ExamCreateRequestDTO entity) throws Exception,NotFoundException;

    ExamViewResponseDTO updateExam (ExamCreateRequestDTO entity,Long id) throws Exception,NotFoundException;

    public byte[] mergeDataIntoTemplate(String templatePath, Map<String, Object> data) throws Exception;

    void mergeDataIntoWord(String templatePath, String outputPath, Map<DataFieldName, String> data) throws Exception;

    public void mergeDataToWord(String templatePath, String outputPath, Map<String, String> data) throws FileNotFoundException, IOException, InvalidFormatException, Exception;
}
