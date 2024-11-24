package com.CodeEvalCrew.AutoScore.services.student_service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeEvalCrew.AutoScore.mappers.StudentMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceDetailRepository;
import com.CodeEvalCrew.AutoScore.repositories.student_repository.StudentRepository;

@Service
public class StudentService implements IStudentService {

    private final StudentRepository studentRepository;
    private final SourceDetailRepository sourceDetailRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, SourceDetailRepository sourceDetailRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.sourceDetailRepository = sourceDetailRepository;
        this.studentMapper = studentMapper;
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
    public List<StudentDTO> getAllStudentOfSource(Long sourceId) {
        List<StudentDTO> result = new ArrayList<>();
        try {
            List<Source_Detail> sourceDetails = sourceDetailRepository.findBySource_ExamPaper_ExamPaperIdOrderByStudent_StudentId(sourceId);

            if(sourceDetails.isEmpty()) throw new NoSuchElementException("No source found");

            for (Source_Detail sourceDetail : sourceDetails) {
                result.add(studentMapper.toDTO(sourceDetail.getStudent()));
            }   

            return result;
        } catch (NoSuchElementException e) {
            throw e;
        }
    }
    
}
