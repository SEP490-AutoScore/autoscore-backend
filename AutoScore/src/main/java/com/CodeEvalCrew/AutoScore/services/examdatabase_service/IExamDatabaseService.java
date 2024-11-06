package com.CodeEvalCrew.AutoScore.services.examdatabase_service;

import org.springframework.web.multipart.MultipartFile;

public interface IExamDatabaseService {
    String importSqlFile(MultipartFile file, MultipartFile imageFile,  Long examPaperId) throws Exception;
}
