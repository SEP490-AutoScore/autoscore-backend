package com.CodeEvalCrew.AutoScore.services.source_service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Log;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.log_repository.LogRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

import jakarta.transaction.Transactional;

@Service
public class SourceService {

    private static final Logger logger = LoggerFactory.getLogger(SourceService.class);

    @Autowired
    private SourceRepository sourceRepo;

    @Autowired
    private IExamPaperRepository examPaperRepo;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private IExamPaperRepository examPaperRepository;

    private void saveLog(Long examPaperId, String actionDetail) {

        Optional<Exam_Paper> optionalExamPaper = examPaperRepository.findById(examPaperId);
        if (optionalExamPaper.isEmpty()) {
            throw new IllegalArgumentException("Exam Paper with ID " + examPaperId + " does not exist.");
        }

        Exam_Paper examPaper = optionalExamPaper.get();
        Log log = examPaper.getLog();

        if (log == null) {
            log = new Log();
            log.setExamPaper(examPaper);
            log.setAllData(actionDetail);
        } else {

            String updatedData = log.getAllData() == null ? "" : log.getAllData() + ", ";
            log.setAllData(updatedData + actionDetail);
        }

        logRepository.save(log);
    }

    @Transactional
    public Source saveMainSource(String path, Long examPaperId) {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        try {
            Optional<Exam_Paper> examPaper = examPaperRepo.findById(examPaperId);

            Exam_Paper examPaper2 = examPaper.get();

            // Lưu thông tin nguồn
            Source source = new Source();
            source.setOriginSourcePath(path);
            source.setExamPaper(examPaper.get());
            source.setImportTime(new java.sql.Timestamp(System.currentTimeMillis()));
            Source savedSource = sourceRepo.save(source);
            logger.info("Successfully saved MAIN SOURCE");

            saveLog(examPaper2.getExamPaperId(), "Account [" + authenticatedUserId
                    + "] [Import source code student successfully] at [" + time + "]");

            return savedSource;
        } catch (DataAccessException e) {
            logger.error("Database error: {}", e.getMessage());
            throw new RuntimeException("Failed to save student submission: " + e.getMessage());
        }
    }
}
