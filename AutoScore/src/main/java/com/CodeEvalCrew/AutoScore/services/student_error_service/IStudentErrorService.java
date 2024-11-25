package com.CodeEvalCrew.AutoScore.services.student_error_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentErrorResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;

public interface IStudentErrorService {

    void saveStudentError(Source source, Student student, String errorContent);

    List<StudentErrorResponseDTO> getStudentErrorBySourceId(Long sourceId);
}
