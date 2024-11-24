package com.CodeEvalCrew.AutoScore.services.student_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;

public interface IStudentService {
    void saveStudents(List<Student> students);

    List<StudentDTO> getAllStudentOfSource(Long sourceId);
}
