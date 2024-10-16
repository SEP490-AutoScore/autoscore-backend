package com.CodeEvalCrew.AutoScore.services.student_error_service;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.models.Entity.Student_Error;
import com.CodeEvalCrew.AutoScore.repositories.student_error_repository.StudentErrorRepository;

@Service
public class StudentErrorService implements IStudentErrorService {
    private final StudentErrorRepository studentErrorRepository;

    public StudentErrorService(StudentErrorRepository studentErrorRepository) {
        this.studentErrorRepository = studentErrorRepository;
    }

    @Override
    public void saveStudentError(Source source, Student student, String errorContent) {
        Student_Error studentError = new Student_Error();
        studentError.setSource(source);
        studentError.setStudent(student);
        studentError.setErrorContent(errorContent);
        studentErrorRepository.save(studentError);
    }
    
}
