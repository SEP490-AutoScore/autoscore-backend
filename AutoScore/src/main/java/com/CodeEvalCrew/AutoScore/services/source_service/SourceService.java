package com.CodeEvalCrew.AutoScore.services.source_service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;

import jakarta.transaction.Transactional;

@Service
public class SourceService {

    private static final Logger logger = LoggerFactory.getLogger(SourceService.class);

    @Autowired
    private SourceRepository sourceRepo;

    @Autowired
    private IExamPaperRepository examPaperRepo;

    @Transactional
    public Source saveMainSource(String path, Long examPaperId) {
        try {
            Optional<Exam_Paper> examPaper = examPaperRepo.findById(examPaperId);

            // Lưu thông tin nguồn
            Source source = new Source();
            source.setOriginSourcePath(path);
            source.setExamPaper(examPaper.get());
            source.setImportTime(new java.sql.Timestamp(System.currentTimeMillis()));
            Source savedSource = sourceRepo.save(source);
            logger.info("Successfully saved MAIN SOURCE");

            return savedSource;
        } catch (DataAccessException e) {
            logger.error("Database error: {}", e.getMessage());
            throw new RuntimeException("Failed to save student submission: " + e.getMessage());
        }
    }
}


