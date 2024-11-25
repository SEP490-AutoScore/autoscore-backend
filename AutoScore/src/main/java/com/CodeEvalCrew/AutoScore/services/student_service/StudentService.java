package com.CodeEvalCrew.AutoScore.services.student_service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeEvalCrew.AutoScore.mappers.StudentMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceDetailRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;
import com.CodeEvalCrew.AutoScore.repositories.student_repository.StudentRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class StudentService implements IStudentService {

    private final StudentRepository studentRepository;
    private final SourceDetailRepository sourceDetailRepository;
    private final SourceRepository sourceRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, SourceDetailRepository sourceDetailRepository, StudentMapper studentMapper,SourceRepository sourceRepository) {
        this.studentRepository = studentRepository;
        this.sourceDetailRepository = sourceDetailRepository;
        this.studentMapper = studentMapper;
        this.sourceRepository = sourceRepository;
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

    @Override
    public List<StudentDTO> getAllStudentOfSource(Long examPaperId) {
        List<StudentDTO> result = new ArrayList<>();
        try {
            Optional<Source> optionalSource = sourceRepository.findByExamPaper_ExamPaperId(examPaperId);
            if(!optionalSource.isPresent()) throw new NoSuchElementException("No source found");

            Source source = optionalSource.get();

            List<Source_Detail> sourceDetails = sourceDetailRepository.findBySource_ExamPaper_ExamPaperIdOrderByStudent_StudentId(examPaperId);

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
