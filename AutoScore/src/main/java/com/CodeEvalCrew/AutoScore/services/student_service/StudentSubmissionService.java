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

import org.hibernate.sql.exec.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private FileExtractionService fileExtractionService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceDetailService sourceDetailService;

    @Autowired
    private StudentErrorService studentErrorService;

    // Số lượng luồng tối đa để xử lý submissions
    private static final int MAX_THREADS = 2;

// Phương thức chính để xử lý file submission
    public List<String> processFileSubmission(MultipartFile file, Long examPaperId) throws IOException {
        List<String> unmatchedStudents = Collections.synchronizedList(new ArrayList<>());

        // Step 1: Giải nén tệp đã upload
        String rootFolder = fileExtractionService.extract7zWithApacheCommons(file, uploadDir);

        // Step 2: Truy cập thư mục StudentSolution
        String mainSourcePath = uploadDir + "\\" + rootFolder;
        File studentSolutionFolder = new File(mainSourcePath + "\\StudentSolution");

        // Lưu main source path vào source
        Source source = sourceService.saveMainSource(mainSourcePath, examPaperId);

        if (studentSolutionFolder.exists() && studentSolutionFolder.isDirectory()) {
            File[] subFolders = studentSolutionFolder.listFiles(File::isDirectory);

            // Kiểm tra nếu có thư mục con nào bên trong
            if (subFolders != null && subFolders.length > 0) {
                // Lấy thư mục con đầu tiên (ví dụ: thư mục "1")
                File actualStudentFolder = subFolders[0]; // Thư mục "1" có thể là subFolders[0]

                if (actualStudentFolder.exists() && actualStudentFolder.isDirectory()) {
                    // Lấy danh sách các thư mục sinh viên bên trong actualStudentFolder
                    File[] studentFolders = actualStudentFolder.listFiles(File::isDirectory);
                    // Kiểm tra nếu có thư mục sinh viên nào bên trong
                    if (studentFolders != null && studentFolders.length > 0) {
                        // Sử dụng ExecutorService để quản lý luồng
                        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
                        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);

                        for (File studentFolder : studentFolders) {
                            completionService.submit(new SubmissionTask(studentFolder, source, unmatchedStudents));
                        }

                        // Đợi tất cả các tác vụ hoàn thành
                        int completedTasks = 0;
                        for (int i = 0; i < studentFolders.length; i++) {
                            try {
                                Future<Void> future = completionService.poll(10, TimeUnit.SECONDS); // Chờ tối đa 10 giây
                                if (future != null) {
                                    try {
                                        future.get();
                                    } catch (java.util.concurrent.ExecutionException e) {
                                    } 
                                    completedTasks++;
                                } else {
                                    logger.warn("Timeout waiting for task to complete.");
                                    unmatchedStudents.add("Task timed out");
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                logger.error("Threads are interrupted while waiting for tasks to complete.", e);
                            } catch (ExecutionException e) {
                                logger.error("Task execution failed: {}", e.getCause());
                                unmatchedStudents.add("Task failed");
                            }
                        }

                        if (completedTasks < studentFolders.length) {
                            logger.warn("Not all tasks completed. Total tasks: {}, Completed tasks: {}", studentFolders.length, completedTasks);
                        }

                        // Shutdown ExecutorService
                        executorService.shutdown();
                        try {
                            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                                executorService.shutdownNow();
                            }
                        } catch (InterruptedException e) {
                            executorService.shutdownNow();
                            Thread.currentThread().interrupt();
                        }

                    } else {
                        throw new IOException("No subfolders found inside StudentSolution");
                    }
                } else {
                    throw new IOException("No subfolders found inside StudentSolution");
                }
            } else {
                throw new IOException("No subfolders found inside StudentSolution");
            }
        } else {
            throw new IOException("StudentSolution folder is missing or invalid");
        }

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
            try {
                String studentCode = extractStudentCode(studentFolder.getName());
                if (studentCode.isEmpty()) {
                    logger.warn("Invalid student folder name: {}", studentFolder.getName());
                    unmatchedStudents.add(studentFolder.getName());
                    return null;
                }

                // logger.info("Processing student folder: {}", studentCode);
                Optional<Student> studentOpt = studentRepository.findByStudentCode(studentCode);

                if (studentOpt.isPresent()) {
                    Student student = studentOpt.get();
                    // Lấy danh sách các thư mục con trong studentFolder
                    File[] studentSubFolders = studentFolder.listFiles(File::isDirectory);
                    if (studentSubFolders != null && studentSubFolders.length > 0) {
                        File studentSubFolder = studentSubFolders[0];
                        // Truy cập folder "0" và giải nén solution.zip
                        File solutionZip = new File(studentSubFolder, "solution.zip");
                        if (solutionZip.exists()) {
                            // Giải nén tệp zip và tất cả các tệp nén lồng nhau
                            File extractedFolder = extractArchive(solutionZip, source, student);
                            if (extractedFolder != null) {
                                // Tiếp tục giải nén các tệp trong thư mục được giải nén
                                File slnFile = processExtractedFolder(extractedFolder, source, student);

                                if (slnFile != null) {
                                    sourceDetailService.saveStudentSubmission(slnFile.getParentFile(), student, source);
                                    // logger.info("Submission saved for student: {}", studentCode);
                                } else {
                                    logger.warn("No .sln file found for student: {}", studentCode);
                                    studentErrorService.saveStudentError(source, student, "No .sln file found");
                                }
                            }
                        } else {
                            logger.warn("solution.zip not found for student: {}", studentCode);
                            studentErrorService.saveStudentError(source, student, "solution.zip not found");
                        }
                    } else {
                        logger.warn("No subfolders found inside student folder: {}", studentCode);
                    }
                } else {
                    studentErrorService.saveStudentError(source, null, "Student not found with student code: " + studentCode);
                    logger.warn("Unmatched student: {}", studentCode);
                }
            } catch (IOException e) {
                logger.error("Error processing folder {}: {}", studentFolder.getName(), e.getMessage());
            }
            return null;
        }
    }

    private String extractStudentCode(String folderName) {
        // Lấy 8 ký tự cuối cùng của tên folder
        if (folderName.length() >= 8) { // Đảm bảo tên folder có ít nhất 8 ký tự
            return folderName.substring(folderName.length() - 8); // Lấy 8 ký tự cuối
        }
        // Nếu tên folder không đạt đủ độ dài, trả về chuỗi rỗng hoặc xử lý ngoại lệ tùy ý
        logger.warn("Invalid folder name: {}", folderName);
        return "";
    }

    // Cập nhật phương thức extractArchive để giải nén tại vị trí hiện tại và xóa tệp nén sau khi giải nén
    private File extractArchive(File archive, Source source, Student student) throws IOException {
        File extractToDir = archive.getParentFile(); // Giải nén tại vị trí hiện tại của tệp nén

        // Kiểm tra loại tệp nén và giải nén tương ứng
        if (archive.getName().endsWith(".zip")) {
            fileExtractionService.extractZipWithZip4j(archive, extractToDir, source, student); // Giải nén tại .zip vị trí hiện tại
        } else if (archive.getName().endsWith(".rar")) {
            fileExtractionService.extractRarWith7ZipCommand(archive, extractToDir, source, student); // Giải nén tại .rar vị trí hiện tại
        } else if (archive.getName().endsWith(".7z")) {
            fileExtractionService.extract7zFile(archive, extractToDir, source, student); // Giải nén tệp .7z tại vị trí hiện tại
        } else {
            fileExtractionService.extractWithCommonsCompress(archive, extractToDir, source, student); // Giải nén khác tại vị trí hiện tại
        }

        // Xóa tệp nén sau khi giải nén
        if (archive.exists() && !archive.delete()) {
            logger.warn("Failed to delete archive: {}", archive.getPath());
        }

        return extractToDir.exists() ? extractToDir : null;
    }

    // Xử lý thư mục đã giải nén: tiếp tục giải nén nếu có tệp nén bên trong, hoặc tìm tệp .sln.
    private File processExtractedFolder(File folder, Source source, Student student) throws IOException {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Xử lý đệ quy nếu có thư mục con
                    File slnFile = processExtractedFolder(file, source, student);
                    if (slnFile != null) {
                        return slnFile;  // Trả về tệp .sln nếu tìm thấy
                    }
                } else if (file.getName().endsWith(".sln")) {
                    return file;  // Tìm thấy tệp .sln
                } else if (file.getName().endsWith(".zip") || file.getName().endsWith(".rar") || file.getName().endsWith(".7z")) {
                    // Nếu còn tệp nén, tiếp tục giải nén tại vị trí hiện tại của tệp nén
                    File extractedFolder = extractArchive(file, source, student);
                    if (extractedFolder != null) {
                        return processExtractedFolder(extractedFolder, source, student);  // Tiếp tục xử lý thư mục vừa giải nén
                    }
                }
            }
        }

        return null;  // Không tìm thấy tệp .sln
    }
}
