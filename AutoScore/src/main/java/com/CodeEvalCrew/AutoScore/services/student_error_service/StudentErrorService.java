package com.CodeEvalCrew.AutoScore.services.student_error_service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.StudentErrorMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentErrorResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.models.Entity.Student_Error;
import com.CodeEvalCrew.AutoScore.repositories.student_error_repository.StudentErrorRepository;

import jakarta.transaction.Transactional;

@Service
public class StudentErrorService implements IStudentErrorService {

    private final StudentErrorRepository studentErrorRepository;
    private final StudentErrorMapper studentErrorMapper;

    public StudentErrorService(StudentErrorRepository studentErrorRepository, StudentErrorMapper studentErrorMapper) {
        this.studentErrorRepository = studentErrorRepository;
        this.studentErrorMapper = studentErrorMapper;
    }

    @Override
    public void saveStudentError(Source source, Student student, String errorContent) {
        Student_Error studentError = new Student_Error();
        studentError.setSource(source);
        studentError.setStudent(student);
        studentError.setErrorContent(errorContent);
        studentErrorRepository.save(studentError);
    }

    @Override
    public List<StudentErrorResponseDTO> getStudentErrorBySourceId(Long sourceId) {
        try {
            List<Student_Error> studentError = studentErrorRepository.findBySourceSourceId(sourceId);
            if (studentError != null) {
                return studentErrorMapper.studentErrorEntityToStudentErrorResponseDTO(studentError);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void deleteStudentErrorBySourceId(Long sourceId) {
        studentErrorRepository.deleteAllBySourceSourceId(sourceId);
    }
}
