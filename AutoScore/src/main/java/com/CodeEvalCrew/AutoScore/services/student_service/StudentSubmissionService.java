package com.CodeEvalCrew.AutoScore.services.student_service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
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

    @Value("${main.source.name}")
    private String mainSourceName;

    @Value("${student.code.regex}")
    private String studentCodeRegex;

    private final FileExtractionService fileExtractionService;
    private final StudentRepository studentRepository;
    private final SourceService sourceService;
    private final SourceDetailService sourceDetailService;
    private final StudentErrorService studentErrorService;

    public StudentSubmissionService(FileExtractionService fileExtractionService,
            StudentRepository studentRepository, SourceService sourceService,
            SourceDetailService sourceDetailService, StudentErrorService studentErrorService) {
        this.fileExtractionService = fileExtractionService;
        this.studentRepository = studentRepository;
        this.sourceService = sourceService;
        this.sourceDetailService = sourceDetailService;
        this.studentErrorService = studentErrorService;
    }

    // Số lượng luồng tối đa để xử lý submissions
    private static final int MAX_THREADS = 4;

    // Phương thức chính để xử lý file submission
    public List<String> processFileSubmission(MultipartFile file, Long examPaperId) throws IOException {
        List<String> unmatchedStudents = Collections.synchronizedList(new ArrayList<>());

        // Giải nén tệp đã upload và lấy thư mục gốc
        String rootFolder = fileExtractionService.extract7zWithApacheCommons(file, uploadDir);
        String mainSourcePath = rootFolder + File.separator + mainSourceName;
        File studentSolutionFolder = new File(mainSourcePath);

        Source source = sourceService.saveMainSource(mainSourcePath, examPaperId);

        if (!studentSolutionFolder.exists() || !studentSolutionFolder.isDirectory()) {
            throw new IOException("StudentSolution folder is missing or invalid");
        }

        File[] studentFolders = studentSolutionFolder.listFiles(File::isDirectory);
        if (studentFolders == null || studentFolders.length == 0) {
            throw new IOException("No subfolders found inside StudentSolution");
        }

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);

        for (File studentFolder : studentFolders) {
            completionService.submit(new SubmissionTask(studentFolder, source, unmatchedStudents));
        }

        for (int i = 0; i < studentFolders.length; i++) {
            try {
                Future<Void> future = completionService.poll(10, TimeUnit.SECONDS);
                if (future != null) {
                    future.get();
                } else {
                    unmatchedStudents.add("Task timed out");
                }
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                unmatchedStudents.add("Task failed");
                logger.error("Task execution failed", e);
            }
        }

        executorService.shutdown();
        return unmatchedStudents;
    }

    // Lớp tác vụ để xử lý từng submission
    private class SubmissionTask implements Callable<Void> {
        private final File studentFolder;
        private final Source source;
        private final List<String> unmatchedStudents;

        public SubmissionTask(File studentFolder, Source source, List<String> unmatchedStudents) {
            this.studentFolder = studentFolder;
            this.source = source;
            this.unmatchedStudents = unmatchedStudents;
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
                    sourceDetailService.saveStudentSubmission(slnFileFolder, studentOpt.get(), source);
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