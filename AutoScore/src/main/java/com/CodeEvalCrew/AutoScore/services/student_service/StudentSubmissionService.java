package com.CodeEvalCrew.AutoScore.services.student_service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Status_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Log;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.log_repository.LogRepository;
import com.CodeEvalCrew.AutoScore.repositories.student_repository.StudentRepository;
import com.CodeEvalCrew.AutoScore.services.file_service.FileExtractionService;
import com.CodeEvalCrew.AutoScore.services.source_service.SourceDetailService;
import com.CodeEvalCrew.AutoScore.services.source_service.SourceService;
import com.CodeEvalCrew.AutoScore.services.student_error_service.StudentErrorService;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class StudentSubmissionService {

    @Autowired
    private LogRepository logRepository;

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

    private static final Logger logger = LoggerFactory.getLogger(StudentSubmissionService.class);

    @Value("${upload.folder}")
    private String uploadDir;

    @Value("${student.code.regex}")
    private String studentCodeRegex;

    private final FileExtractionService fileExtractionService;
    private final StudentRepository studentRepository;
    private final SourceService sourceService;
    private final SourceDetailService sourceDetailService;
    private final StudentErrorService studentErrorService;
    private final IExamPaperRepository examPaperRepository;
    private final FileProcessingProgressService progressService;
    private static final int MAX_THREADS = 4;

    AtomicInteger totalTasks = new AtomicInteger(0);
    AtomicInteger completedTasks = new AtomicInteger(0);
    AtomicInteger failedTasks = new AtomicInteger(0); // Đếm số lượng tác vụ thất bại

    public StudentSubmissionService(FileExtractionService fileExtractionService,
            StudentRepository studentRepository, SourceService sourceService,
            SourceDetailService sourceDetailService, StudentErrorService studentErrorService,
            IExamPaperRepository examPaperRepository, FileProcessingProgressService progressService) {
        this.fileExtractionService = fileExtractionService;
        this.studentRepository = studentRepository;
        this.sourceService = sourceService;
        this.sourceDetailService = sourceDetailService;
        this.studentErrorService = studentErrorService;
        this.examPaperRepository = examPaperRepository;
        this.progressService = progressService;
    }

    // Phương thức chính để xử lý file submission
    public List<String> processFileSubmission(MultipartFile file, Long examId) throws IOException {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        List<String> unmatchedStudents = Collections.synchronizedList(new ArrayList<>());
        List<String> errors = Collections.synchronizedList(new ArrayList<>()); // Danh sách lưu lỗi
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);

        try {
            // Giải nén file
            String rootFolder = fileExtractionService.extract7zWithApacheCommons(file, uploadDir);
            File rootDirectory = new File(rootFolder);

            if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
                logger.error("Root directory does not exist or is not a directory: {}", rootDirectory.getAbsolutePath());
                progressService.sendProgress(100); // Hoàn tất vì không thể xử lý
                return unmatchedStudents;
            }

            // Lấy danh sách folder tầng 2
            File[] examFolders = rootDirectory.listFiles(File::isDirectory);

            if (examFolders == null || examFolders.length == 0) {
                logger.error("No subfolders found inside root directory: {}", rootDirectory.getAbsolutePath());
                progressService.sendProgress(100); // Hoàn tất vì không có gì để xử lý
                return unmatchedStudents;
            }

            int totalExamFolders = examFolders.length;
            AtomicInteger completedExamFolders = new AtomicInteger(0);

            for (File examFolder : examFolders) {
                Optional<Exam_Paper> examPaper = examPaperRepository.findByExamPaperCode(examFolder.getName());

                if (!examPaper.isPresent() || !examPaper.get().getExam().getExamId().equals(examId) || !examPaper.get().getIsUsed() || !examPaper.get().getStatus().equals(Exam_Status_Enum.COMPLETE)) {
                    errors.add("No matching exam paper found for folder: " + examFolder.getName());
                    completedExamFolders.incrementAndGet();
                    progressService.sendProgress((completedExamFolders.get() * 100) / totalExamFolders); // Tiến độ theo folder
                    continue;
                }
              
                Exam_Paper foundExamPaper = examPaper.get();
                Source source = sourceService.saveMainSource(examFolder.getAbsolutePath(), examPaper.get());

                // Xử lý các studentFolders bên trong examFolder
                processStudentFolders(examFolder, examPaper.get(), unmatchedStudents, errors, completionService, source);

                for (File studentFolder : studentFolders) {
                    totalTasks.incrementAndGet();
                    completionService.submit(() -> {
                        try {
                            completionService.submit(new SubmissionTask(studentFolder, source, unmatchedStudents,
                                    examPaper.get().getExam().getType().toString()));
                        } catch (Exception e) {
                            errors.add("Error processing folder " + studentFolder.getName() + ": " + e.getMessage());
                            logger.error("Error processing folder: " + studentFolder.getName(), e);
                        } finally {
                            int progress = (completedTasks.incrementAndGet() * 100) / totalTasks.get();
                            progressService.sendProgress(progress);
                        }
                        return null;
                    });
                }

                saveLog(foundExamPaper.getExamPaperId(),
                        "Account [" + authenticatedUserId + "] [Import list student successfully] at [" + time + "]");

                completedExamFolders.incrementAndGet();
                progressService.sendProgress((completedExamFolders.get() * 100) / totalExamFolders); // Cập nhật tiến trình tổng
            }

            // Chờ tất cả các tác vụ hoàn thành
            while (completedTasks.get() + failedTasks.get() < totalTasks.get()) {
                try {
                    Future<Void> future = completionService.poll(10, TimeUnit.SECONDS); // Chờ tối đa 10 giây
                    if (future != null) {
                        try {
                            future.get(); // Lấy kết quả nếu không có lỗi
                            completedTasks.incrementAndGet(); // Đánh dấu tác vụ thành công
                        } catch (ExecutionException | InterruptedException e) {
                            errors.add("Task execution error: " + e.getMessage());
                            logger.error("Error while processing tasks", e);
                            failedTasks.incrementAndGet(); // Đánh dấu tác vụ thất bại
                        }
                    } else {
                        logger.warn("No task completed in the last 5 seconds. Retrying...");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errors.add("Interrupted while waiting for task completion");
                    logger.error("Interrupted exception", e);
                    break; // Thoát vòng lặp nếu bị ngắt
                }
            }

            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            if (completedTasks.get() + failedTasks.get() >= totalTasks.get()) {
                progressService.sendProgress(100);
            }
        } catch (IOException | InterruptedException e) {
            errors.add("Critical error: " + e.getMessage());
            logger.error("Critical error while processing submission", e);
        } finally {
            executorService.shutdownNow();
        }

        // Kết hợp danh sách lỗi và unmatchedStudents để trả về
        unmatchedStudents.addAll(errors);
        return unmatchedStudents;
    }

    private void processStudentFolders(File examFolder, Exam_Paper examPaper, List<String> unmatchedStudents,
            List<String> errors, CompletionService<Void> completionService, Source source) {

        File[] studentFolders = examFolder.listFiles(File::isDirectory);
        if (studentFolders == null || studentFolders.length == 0) {
            totalTasks.incrementAndGet();
            errors.add("No subfolders found inside " + examFolder.getName());
            completedTasks.incrementAndGet();
            progressService.sendProgress((completedTasks.get() * 100) / totalTasks.get());
            return;
        }

        for (File studentFolder : studentFolders) {
            logger.info("Processing student folder: {}", studentFolder.getAbsolutePath());
            totalTasks.incrementAndGet();
            completionService.submit(() -> {
                try {
                    new SubmissionTask(studentFolder, source,
                            unmatchedStudents, examPaper.getExam().getType().toString(), errors, examPaper.getExam().getExamId()).call();
                } catch (Exception e) {
                    errors.add("Error processing folder " + studentFolder.getName() + ": " + e.getMessage());
                    logger.error("Error processing folder: " + studentFolder.getName(), e);
                    failedTasks.incrementAndGet();
                } finally {
                    synchronized (this) {
                        int totalProgress = completedTasks.get() + failedTasks.get();
                        if (totalProgress < totalTasks.get()) {
                            int progress = (totalProgress * 100) / totalTasks.get();
                            progressService.sendProgress(progress);
                        }
                    }
                }
                return null;
            });
        }
    }

    // Lớp tác vụ để xử lý từng submission
    private class SubmissionTask implements Callable<Void> {

        private final File studentFolder;
        private final Source source;
        private final List<String> unmatchedStudents;
        private final String examType;
        private final List<String> errors;
        private final Long examId;

        public SubmissionTask(File studentFolder, Source source, List<String> unmatchedStudents, String examType, List<String> errors, Long examId) {
            this.studentFolder = studentFolder;
            this.source = source;
            this.unmatchedStudents = unmatchedStudents;
            this.examType = examType;
            this.errors = errors;
            this.examId = examId;
        }

        @Override
        public Void call() {
            String studentCode = extractStudentCode(studentFolder.getName());
            if (studentCode.isEmpty()) {
                unmatchedStudents.add(studentFolder.getName());
                failedTasks.incrementAndGet(); // Đánh dấu thất bại
                return null;
            }

            Optional<Student> studentOpt = studentRepository.findByStudentCodeAndExamExamId(studentCode, examId);
            if (!studentOpt.isPresent()) {
                studentErrorService.saveStudentError(source, null, "Student not found with student code: " + studentCode);
                failedTasks.incrementAndGet(); // Đánh dấu thất bại
                return null;
            }

            try {
                // Giải nén đệ quy và tìm thư mục chứa .sln
                File slnFileFolder = fileExtractionService.processExtractedFolder(studentFolder, source, studentOpt.get());
                if (slnFileFolder != null) {
                    sourceDetailService.saveStudentSubmission(slnFileFolder, studentOpt.get(), source, examType);
                } else {
                    String error = "No .sln file found for student folder: " + studentFolder.getName();
                    errors.add(error);
                    studentErrorService.saveStudentError(source, studentOpt.get(), error);
                    failedTasks.incrementAndGet();
                }
            } catch (IOException e) {
                String error = "Extraction error for folder " + studentFolder.getName() + ": " + e.getMessage();
                errors.add(error);
                studentErrorService.saveStudentError(source, studentOpt.get(), error);
                failedTasks.incrementAndGet();
            }
            return null;
        }
    }

    private String extractStudentCode(String folderName) {
        Pattern pattern = Pattern.compile(studentCodeRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(folderName);
        if (matcher.find()) {
            return matcher.group().toLowerCase();
        }
        return "";
    }

    public AtomicInteger getTotalTasks() {
        return totalTasks;
    }

    public AtomicInteger getCompletedTasks() {
        return completedTasks;
    }

    public AtomicInteger getFailedTasks() {
        return failedTasks;
    }
}
