package com.CodeEvalCrew.AutoScore.services.source_service;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceDetailsResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourcesResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentErrorResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Log;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.log_repository.LogRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;
import com.CodeEvalCrew.AutoScore.services.student_error_service.StudentErrorService;
import com.CodeEvalCrew.AutoScore.utils.Util;

import jakarta.transaction.Transactional;

@Service
public class SourceService {

    private static final Logger logger = LoggerFactory.getLogger(SourceService.class);
    private final SourceRepository sourceRepo;
    private final IExamPaperRepository examPaperRepo;
    private final SourceDetailService sourceDetailService;
    private final StudentErrorService studentErrorService;
    private final LogRepository logRepository;
    private final IExamPaperRepository examPaperRepository;

    @Value("${upload.folder}")
    private String uploadFolder;

    public SourceService(SourceRepository sourceRepo, IExamPaperRepository examPaperRepo, SourceDetailService sourceDetailService,
            StudentErrorService studentErrorService, LogRepository logRepository, IExamPaperRepository examPaperRepository) {
        this.sourceRepo = sourceRepo;
        this.examPaperRepo = examPaperRepo;
        this.sourceDetailService = sourceDetailService;
        this.studentErrorService = studentErrorService;
        this.logRepository = logRepository;
        this.examPaperRepository = examPaperRepository;
    }

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

    public Source saveMainSource(String path, Exam_Paper examPaper) {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();
        try {
            // Kiểm tra nếu nguồn đã tồn tại
            Optional<Source> existingSource = sourceRepo.findByOriginSourcePath(path);
            if (existingSource.isPresent()) {
                Long sourceId = existingSource.get().getSourceId();

                // Xóa chi tiết nguồn và lỗi sinh viên
                sourceDetailService.deleteSourceDetailBySourceId(sourceId);
                studentErrorService.deleteStudentErrorBySourceId(sourceId);
                // Xóa bản ghi source
                deleteSourceById(sourceId);

                // Xóa file và thư mục liên quan
                // Path folderPath = Paths.get(path);
                // if (Files.exists(folderPath) && folderPath.toFile().isDirectory()) {
                //     File[] files = folderPath.toFile().listFiles();
                //     if (files != null) {
                //         for (File file : files) {
                //             deleteRecursively(file);
                //         }
                //     }
                // }
            }

            // Tạo mới nguồn và lưu
            Source source = new Source();
            source.setOriginSourcePath(path);
            source.setExamPaper(examPaper);
            source.setImportTime(new Timestamp(System.currentTimeMillis()));
            Source savedSource = sourceRepo.save(source);

            saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                    + "] [Import source code student successfully] at [" + time + "]");

            logger.info("Successfully saved MAIN SOURCE for path: {}", path);
            return savedSource;
        } catch (DataAccessException
        // | IOException
        e) {
            logger.error("Error in saveMainSource: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save main source", e); // Đảm bảo rollback khi có lỗi
        }
    }

    // Hàm hỗ trợ xóa đệ quy các file và thư mục
    private void deleteRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    deleteRecursively(subFile);
                }
            }
        }
        if (!file.delete()) {
            logger.warn("Failed to delete file or directory: " + file.getAbsolutePath());
        }
    }

    public List<SourcesResponseDTO> getAllSourcesByExamId(Long examId) {
        try {
            Optional<List<Exam_Paper>> examPapers = examPaperRepo.findAllByExamExamId(examId);
            List<SourcesResponseDTO> sourcesResponseDTOs = new ArrayList<>();
            if (!examPapers.isPresent()) {
                logger.error("No exam paper found for the given examId: ", examId);
                return null;
            }

            for (Exam_Paper examPaper : examPapers.get()) {
                if (!examPaper.getIsUsed()) {
                    continue;
                }

                Optional<List<Source>> sources = sourceRepo.findAllByExamPaperExamPaperId(examPaper.getExamPaperId());
                if (sources.isEmpty()) {
                    logger.error("No sources found for the given examId: ", examId);
                    return null;
                }

                List<SourceDetailsResponseDTO> sourceDetailsDTOs = new ArrayList<>();
                for (Source source : sources.get()) {
                    List<Source_Detail> sourceDetails = sourceDetailService.getSourceDetailBySourceId(source.getSourceId());
                    if (sourceDetails.isEmpty()) {
                        logger.error("No source details found for the given sourceId: ", source.getSourceId());
                        return null;
                    }
                    for (Source_Detail sourceDetail : sourceDetails) {
                        String sourcePath = sourceDetail.getStudentSourceCodePath().replace(uploadFolder + "\\", "");
                        SourceDetailsResponseDTO sourceDetailsResponseDTO = new SourceDetailsResponseDTO();
                        sourceDetailsResponseDTO.setStudentCode(sourceDetail.getStudent().getStudentCode());
                        sourceDetailsResponseDTO.setStudentEmail(sourceDetail.getStudent().getStudentEmail());
                        sourceDetailsResponseDTO.setSourcePath(sourcePath.replace("\\", "/"));
                        sourceDetailsResponseDTO.setType(sourceDetail.getType().toString());
                        sourceDetailsDTOs.add(sourceDetailsResponseDTO);
                    }

                    List<StudentErrorResponseDTO> studentErrors = studentErrorService.getStudentErrorBySourceId(source.getSourceId());

                    String sourcePath = source.getOriginSourcePath().replace(uploadFolder + "\\", "");
                    SourcesResponseDTO sourcesResponseDTO = new SourcesResponseDTO();
                    sourcesResponseDTO.setExamCode(examPaper.getExam().getExamCode());
                    sourcesResponseDTO.setExamPaperCode(examPaper.getExamPaperCode());
                    sourcesResponseDTO.setSubjectName(examPaper.getSubject().getSubjectName());
                    sourcesResponseDTO.setSubjectCode(examPaper.getSubject().getSubjectCode());
                    sourcesResponseDTO.setSourcePath(sourcePath.replace("\\", "/"));
                    sourcesResponseDTO.setSourceDetails(sourceDetailsDTOs);
                    sourcesResponseDTO.setStudentErrors(studentErrors);
                    sourcesResponseDTOs.add(sourcesResponseDTO);
                }

            }

            return sourcesResponseDTOs;
        } catch (Exception e) {
            logger.error("Get all source details error!");
        }
        return null;
    }

    @Transactional
    private void deleteSourceById(Long sourceId) {
        sourceRepo.deleteById(sourceId);
    }
}
