package com.CodeEvalCrew.AutoScore.services.examdatabase_service;

import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamDatabaseDTO;

public interface IExamDatabaseService {
    String importSqlFile(MultipartFile file, MultipartFile imageFile, Long examPaperId, String databaseNote, String databaseDescription) throws Exception;

    String updateSqlFile(MultipartFile file, MultipartFile imageFile, Long examPaperId, String databaseNote, String databaseDescription) throws Exception;

    // ExamDatabaseDTO getExamDatabaseByExamPaperId(Long examPaperId);
     Optional<ExamDatabaseDTO> getExamDatabaseByExamPaperId(Long examPaperId);
}
