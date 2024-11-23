package com.CodeEvalCrew.AutoScore.services.student_service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.student_repository.StudentRepository;

@Service
public class StudentService implements IStudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional
    public void saveStudents(List<Student> students) {
        for (Student student : students) {
            Optional<Student> existingStudent = studentRepository.findByStudentCode(student.getStudentCode());

            if (existingStudent.isEmpty()) {
                studentRepository.save(student);
            }
        }
    }
}
