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
    private static final int MAX_THREADS = 4;

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

        AtomicInteger totalTasks = new AtomicInteger(0);
        AtomicInteger completedTasks = new AtomicInteger(0);

        try {
            // Giải nén file
            String rootFolder = fileExtractionService.extract7zWithApacheCommons(file, uploadDir);
            File rootDirectory = new File(rootFolder);

            if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
                throw new IOException("Root folder is missing or invalid");
            }

            // Lấy danh sách folder tầng 2
            File[] examFolders = rootDirectory.listFiles(File::isDirectory);
            if (examFolders == null || examFolders.length == 0) {
                throw new IOException("No folders found in the root directory");
            }

            for (File examFolder : examFolders) {
                Optional<Exam_Paper> examPaper = examPaperRepository.findByExamPaperCode(examFolder.getName());
                if (!examPaper.isPresent() || !examPaper.get().getExam().getExamId().equals(examId) || !examPaper.get().getIsUsed() || !examPaper.get().getStatus().equals(Exam_Status_Enum.COMPLETE)) {
                    unmatchedStudents.add("No matching exam paper found for folder: " + examFolder.getName());
                    continue;
                }

                Source source = sourceService.saveMainSource(examFolder.getAbsolutePath(), examPaper.get().getExamPaperId());

                File[] studentFolders = examFolder.listFiles(File::isDirectory);
                if (studentFolders == null || studentFolders.length == 0) {
                    unmatchedStudents.add("No subfolders found inside " + examFolder.getName());
                    continue;
                }

                for (File studentFolder : studentFolders) {
                    totalTasks.incrementAndGet();
                    completionService.submit(() -> {
                        try {
                            completionService.submit(new SubmissionTask(studentFolder, source, unmatchedStudents, examPaper.get().getExam().getType().toString()));
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
            }

            // Chờ tất cả các tác vụ hoàn thành
            while (completedTasks.get() < totalTasks.get()) {
                try {
                    Future<Void> future = completionService.poll(10, TimeUnit.SECONDS);
                    if (future != null) {
                        future.get(); // Bắt lỗi nếu Callable thất bại
                    }
                } catch (InterruptedException | ExecutionException e) {
                    errors.add("Task execution error: " + e.getMessage());
                    logger.error("Error while processing tasks", e);
                }
            }

            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.MINUTES);
            progressService.sendProgress(100); // Tiến trình hoàn tất
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

    // Lớp tác vụ để xử lý từng submission
    private class SubmissionTask implements Callable<Void> {

        private final File studentFolder;
        private final Source source;
        private final List<String> unmatchedStudents;
        private final String examType;

        public SubmissionTask(File studentFolder, Source source, List<String> unmatchedStudents, String examType) {
            this.studentFolder = studentFolder;
            this.source = source;
            this.unmatchedStudents = unmatchedStudents;
            this.examType = examType;
        }

        @Override
        public Void call() {
            String studentCode = extractStudentCode(studentFolder.getName());
            if (studentCode.isEmpty()) {
                unmatchedStudents.add(studentFolder.getName());
                return null;
            }

            Optional<Student> studentOpt = studentRepository.findByStudentCode(studentCode);
            if (!studentOpt.isPresent()) {
                studentErrorService.saveStudentError(source, null, "Student not found with student code: " + studentCode);
                return null;
            }

            try {
                // Giải nén đệ quy và tìm thư mục chứa .sln
                File slnFileFolder = fileExtractionService.processExtractedFolder(studentFolder, source, studentOpt.get());

                if (slnFileFolder != null) {
                    sourceDetailService.saveStudentSubmission(slnFileFolder, studentOpt.get(), source, examType);
                } else {
                    studentErrorService.saveStudentError(source, studentOpt.get(), "No .sln file found");
                }
            } catch (IOException e) {
                studentErrorService.saveStudentError(source, studentOpt.get(), "Extraction error: " + e.getMessage());
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
}
