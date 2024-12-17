package com.CodeEvalCrew.AutoScore.services.source_service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.SourceDetailMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceDetailDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Type_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceDetailRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;

import jakarta.transaction.Transactional;

@Service
public class SourceDetailService {

    private static final Logger logger = LoggerFactory.getLogger(SourceService.class);

    private final SourceDetailRepository sourceDetailRepository;
    private final SourceDetailMapper sourceDetailMapper;
    // private final SourceRepository sourceRepository;

    public SourceDetailService(SourceDetailRepository sourceDetailRepository,
            SourceDetailMapper sourceDetailMapper,
            SourceRepository sourceRepository) {
        this.sourceDetailRepository = sourceDetailRepository;
        this.sourceDetailMapper = sourceDetailMapper;
        // this.sourceRepository = sourceRepository;
    }

    public List<SourceDetailDTO> getAllSourceDetails() {
        return sourceDetailRepository.findAll().stream()
                .map(sourceDetailMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<Source_Detail> getSourceDetailBySourceId(Long sourceId) {
        try {
            List<Source_Detail> sourceDetails = sourceDetailRepository.findAllBySourceSourceId(sourceId);
            if (!sourceDetails.isEmpty()) {
                return sourceDetails;
            }
        } catch (Exception e) {
            logger.error("Get all source details error!");
        }
        return null;
    }

    @Transactional
    public Source_Detail saveStudentSubmission(File studentFolder, Student student, Source source, String examType) {
        try {
            // Lưu thông tin chi tiết về mã nguồn
            Source_Detail sourceDetail = new Source_Detail();
            sourceDetail.setStudentSourceCodePath(studentFolder.getPath());
            sourceDetail.setStudent(student);
            sourceDetail.setSource(source);
            sourceDetail.setType(Exam_Type_Enum.valueOf(examType));
            Source_Detail savedSourceDetail = sourceDetailRepository.save(sourceDetail);
            return savedSourceDetail;
        } catch (DataAccessException e) {
            logger.error("Save student submission error!");
        }
        return null;
    }

    @Transactional
    public void deleteSourceDetailBySourceId(Long sourceId) {
        sourceDetailRepository.deleteAllBySourceSourceId(sourceId);
    }
}
