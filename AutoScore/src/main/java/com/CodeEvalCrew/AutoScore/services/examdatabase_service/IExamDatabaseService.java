package com.CodeEvalCrew.AutoScore.services.examdatabase_service;

import org.springframework.web.multipart.MultipartFile;

public interface IExamDatabaseService {
    String importSqlFile(MultipartFile file, MultipartFile imageFile, Long examPaperId) throws Exception;

    String updateSqlFile(MultipartFile file, MultipartFile imageFile, Long examPaperId) throws Exception; // Thêm phương
                                                                                                          // thức update

}
