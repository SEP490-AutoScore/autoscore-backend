package com.CodeEvalCrew.AutoScore.services.student_service;

import java.io.File;
import java.io.IOException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Status_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.student_repository.StudentRepository;
import com.CodeEvalCrew.AutoScore.services.file_service.FileExtractionService;
import com.CodeEvalCrew.AutoScore.services.source_service.SourceDetailService;
import com.CodeEvalCrew.AutoScore.services.source_service.SourceService;
import com.CodeEvalCrew.AutoScore.services.student_error_service.StudentErrorService;

@Service
public class StudentSubmissionService {

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
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

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

        List<String> unmatchedStudents = Collections.synchronizedList(new ArrayList<>());
        List<String> errors = Collections.synchronizedList(new ArrayList<>()); // Danh sách lưu lỗi
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);

        try {
            // Giải nén file
            String rootFolder = fileExtractionService.extract7zWithApacheCommons(file, uploadDir);
            File rootDirectory = new File(rootFolder);

            if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
                logger.error("Root directory does not exist or is not a directory: {}",
                        rootDirectory.getAbsolutePath());
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

                if (!examPaper.isPresent() || !examPaper.get().getExam().getExamId().equals(examId)
                        || !examPaper.get().getIsUsed()
                        || !examPaper.get().getStatus().equals(Exam_Status_Enum.COMPLETE)) {
                    unmatchedStudents.add("No matching exam paper found for folder: " + examFolder.getName());
                    completedExamFolders.incrementAndGet();
                    progressService.sendProgress((completedExamFolders.get() * 100) / totalExamFolders);
                    continue;
                }

                Source source = sourceService.saveMainSource(examFolder.getAbsolutePath(), examPaper.get());

                // Chỉ xử lý khi các điều kiện đã được thỏa mãn
                processStudentFolders(examFolder, examPaper.get(), unmatchedStudents, errors, completionService, source);

                completedExamFolders.incrementAndGet();
                progressService.sendProgress((completedExamFolders.get() * 100) / totalExamFolders);
            }

            // Chờ tất cả các tác vụ hoàn thành
            while (completedTasks.get() + failedTasks.get() < totalTasks.get()) {
                try {
                    Future<Void> future = completionService.poll(10, TimeUnit.SECONDS);
                    if (future != null) {
                        try {
                            future.get(); // Lấy kết quả nếu không có lỗi
                            completedTasks.incrementAndGet(); // Đánh dấu tác vụ thành công
                        } catch (ExecutionException | InterruptedException e) {
                            errors.add("Task execution error: " + e.getMessage());
                            failedTasks.incrementAndGet(); // Đánh dấu tác vụ thất bại
                        }
                    } else {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errors.add("Interrupted while waiting for task completion");
                    break; // Thoát vòng lặp nếu bị ngắt
                }
            }

            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                    errors.add("Some tasks did not complete within the time limit.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                errors.add("Interrupted while waiting for task completion: " + e.getMessage());
            }
            if (completedTasks.get() + failedTasks.get() >= totalTasks.get()) {
                progressService.sendProgress(100);
            }
        } catch (IOException e) {
            errors.add("Critical error: " + e.getMessage());
        } finally {
            executorService.shutdownNow();
        }

        unmatchedStudents.addAll(errors);
        return unmatchedStudents;
    }

    private void processStudentFolders(File examFolder, Exam_Paper examPaper, List<String> unmatchedStudents,
            List<String> errors, CompletionService<Void> completionService, Source source) {

        File[] studentFolders = examFolder.listFiles(File::isDirectory);
        if (studentFolders == null || studentFolders.length == 0) {
            String error = "No student folders found in " + examFolder.getName();
            unmatchedStudents.add(error);
            failedTasks.incrementAndGet();
            return;
        }

        for (File studentFolder : studentFolders) {
            logger.info("Processing student folder: {}", studentFolder.getAbsolutePath());
            totalTasks.incrementAndGet();
            completionService.submit(() -> {
                try {
                    new SubmissionTask(studentFolder, source, unmatchedStudents, examPaper.getExam().getType().toString(),
                            errors, examPaper.getExam().getExamId()).call();
                    completedTasks.incrementAndGet();
                } catch (Exception e) {
                    errors.add("Error processing folder " + studentFolder.getName() + ": " + e.getMessage());
                    failedTasks.incrementAndGet();
                } finally {
                    synchronized (this) {
                        int totalProgress = completedTasks.get() + failedTasks.get();
                        if (totalProgress <= totalTasks.get()) {
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

        public SubmissionTask(File studentFolder, Source source, List<String> unmatchedStudents, String examType,
                List<String> errors, Long examId) {
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
                unmatchedStudents.add("No valid student code for folder: " + studentFolder.getName());
                failedTasks.incrementAndGet();
                return null;
            }

            Optional<Student> studentOpt = studentRepository.findByStudentCodeAndExamExamId(studentCode, examId);
            if (!studentOpt.isPresent()) {
                String error = "Student not found for code: " + studentCode + " in folder: " + studentFolder.getName();
                unmatchedStudents.add(error);
                studentErrorService.saveStudentError(source, null, error);
                failedTasks.incrementAndGet();
                return null;
            }

            try {
                File slnFileFolder = fileExtractionService.processExtractedFolder(studentFolder, source, studentOpt.get());
                if (slnFileFolder != null) {
                    if (slnFileFolder.length() > 1) {
                        String error = "More than one .sln file found in folder: " + studentFolder.getName();
                        errors.add(error);
                        studentErrorService.saveStudentError(source, studentOpt.get(), error);
                        failedTasks.incrementAndGet();
                    }
                    sourceDetailService.saveStudentSubmission(slnFileFolder, studentOpt.get(), source, examType);
                } else {
                    String error = "No .sln file found in folder: " + studentFolder.getName();
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
}
