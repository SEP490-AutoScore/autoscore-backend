package com.CodeEvalCrew.AutoScore.services.student_service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.student_repository.StudentRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

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

    @Override
    public List<StudentResponseDTO> getAllStudents(Long examId) {
        try {
            String campusName = Util.getCampus();
             List<StudentResponseDTO> studentResponseDTOs = new ArrayList<>();
            if (campusName != null) {
                List<Student> students = studentRepository.findAllByExamExamIdAndOrganizationName(examId, campusName).get();
                for (Student student : students) {
                    StudentResponseDTO studentResponseDTO = new StudentResponseDTO();
                    studentResponseDTO.setStudentCode(student.getStudentCode());
                    studentResponseDTO.setStudentEmail(student.getStudentEmail());
                    studentResponseDTO.setExamCode(student.getExam().getExamCode());
                    studentResponseDTO.setCampus(campusName);
                    studentResponseDTOs.add(studentResponseDTO);
                }
            }
            return studentResponseDTOs;
        } catch (Exception e) {
            return null;
        }
    }
}
