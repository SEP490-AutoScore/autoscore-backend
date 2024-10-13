package com.CodeEvalCrew.AutoScore.services.source_service;

import com.CodeEvalCrew.AutoScore.mappers.SourceDetailMapper;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceDetailRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;
import com.CodeEvalCrew.AutoScore.repositories.student_repository.StudentRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceDetailDTO;


@Service
public class SourceDetailService {

    private final SourceDetailRepository sourceDetailRepository;
    private final SourceDetailMapper sourceDetailMapper;
    private final StudentRepository studentRepository;
    private final SourceRepository sourceRepository;

    public SourceDetailService(SourceDetailRepository sourceDetailRepository,
                               SourceDetailMapper sourceDetailMapper,
                               StudentRepository studentRepository,
                               SourceRepository sourceRepository) {
        this.sourceDetailRepository = sourceDetailRepository;
        this.sourceDetailMapper = sourceDetailMapper;
        this.studentRepository = studentRepository;
        this.sourceRepository = sourceRepository;
    }

    public List<SourceDetailDTO> getAllSourceDetails() {
        return sourceDetailRepository.findAll().stream()
            .map(sourceDetailMapper::toDTO)
            .collect(Collectors.toList());
    }

    public SourceDetailDTO getSourceDetailById(Long id) {
        Source_Detail sourceDetail = sourceDetailRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Source detail not found"));
        return sourceDetailMapper.toDTO(sourceDetail);
    }

    public SourceDetailDTO createSourceDetail(SourceDetailDTO dto) {
        Source_Detail sourceDetail = sourceDetailMapper.toEntity(dto);

        // Lấy thông tin student và source dựa trên ID và set vào entity
        Optional<Student> student = studentRepository.findById(dto.getStudentId());
        Optional<Source> source = sourceRepository.findById(dto.getSourceId());

        if (student.isPresent() && source.isPresent()) {
            sourceDetail.setStudent(student.get());
            sourceDetail.setSource(source.get());
        } else {
            throw new RuntimeException("Invalid student or source ID");
        }

        sourceDetail = sourceDetailRepository.save(sourceDetail);
        return sourceDetailMapper.toDTO(sourceDetail);
    }

    public void deleteSourceDetail(Long id) {
        sourceDetailRepository.deleteById(id);
    }
}
