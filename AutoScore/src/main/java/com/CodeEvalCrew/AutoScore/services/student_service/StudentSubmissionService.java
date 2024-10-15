package com.CodeEvalCrew.AutoScore.services.student_service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
                        // Xử lý tất cả các thư mục sinh viên
                        Arrays.stream(studentFolders).parallel().forEach(studentFolder -> {
                            try {
                                String studentCode = extractStudentCode(studentFolder.getName());
                                logger.info("Processing student folder: {}", studentCode);

                                Optional<Student> student = studentRepository.findByStudentCode(studentCode);

                                if (student.isPresent()) {
                                    // Lấy danh sách các thử mục bên trong studentFolder
                                    File[] studentSubFolders = studentFolder.listFiles(File::isDirectory);
                                    if (studentSubFolders != null && studentSubFolders.length > 0) {
                                        File studentSubFolder = studentSubFolders[0];
                                        // Truy cập folder "0" và giải nén solution.zip
                                        File solutionZip = new File(studentSubFolder, "solution.zip");
                                        if (solutionZip.exists()) {
                                            // Giải nén tệp zip và tất cả các tệp nén lồng nhau
                                            File extractedFolder = extractArchive(solutionZip);
                                            if (extractedFolder != null) {
                                                // Tiếp tục giải nén các tệp trong thư mục được giải nén
                                                File slnFile = processExtractedFolder(extractedFolder);

                                                if (slnFile != null) {
                                                    sourceDetailService.saveStudentSubmission(slnFile, student.get(), source);
                                                    logger.info("Submission saved for student: {}", studentCode);
                                                } else {
                                                    logger.warn("No .sln file found for student: {}", studentCode);
                                                    unmatchedStudents.add(studentCode);
                                                }
                                            }
                                        } else {
                                            logger.warn("solution.zip not found for student: {}", studentCode);
                                            unmatchedStudents.add(studentCode);
                                        }
                                    } else {
                                        logger.warn("No subfolders found inside StudentSolution");
                                    }
                                } else {
                                    unmatchedStudents.add(studentCode);
                                    logger.warn("Unmatched student: {}", studentCode);
                                }
                            } catch (IOException e) {
                                logger.error("Error processing folder {}: {}", actualStudentFolder.getName(), e.getMessage());
                            }
                        });
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
    private File extractArchive(File archive) throws IOException {
        File extractToDir = archive.getParentFile(); // Giải nén tại vị trí hiện tại của tệp nén

        // Kiểm tra loại tệp nén và giải nén tương ứng
        if (archive.getName().endsWith(".zip")) {
            fileExtractionService.extractZipWithZip4j(archive, extractToDir); // Giải nén tại vị trí hiện tại
        } else if (archive.getName().endsWith(".rar")) {
            fileExtractionService.extractWithCommonsCompress(archive, extractToDir); // Giải nén tại vị trí hiện tại
        } else if (archive.getName().endsWith(".7z")) {
            fileExtractionService.extract7zFile(archive, extractToDir); // Giải nén tệp .7z tại vị trí hiện tại
        } else {
            throw new IOException("Unsupported archive format: " + archive.getName());
        }

        // Xóa tệp nén sau khi giải nén
        if (archive.exists() && !archive.delete()) {
            logger.warn("Failed to delete archive: {}", archive.getPath());
        }

        return extractToDir.exists() ? extractToDir : null;
    }

    // Xử lý thư mục đã giải nén: tiếp tục giải nén nếu có tệp nén bên trong, hoặc tìm tệp .sln.
    private File processExtractedFolder(File folder) throws IOException {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Xử lý đệ quy nếu có thư mục con
                    File slnFile = processExtractedFolder(file);
                    if (slnFile != null) {
                        return slnFile;  // Trả về tệp .sln nếu tìm thấy
                    }
                } else if (file.getName().endsWith(".sln")) {
                    return file;  // Tìm thấy tệp .sln
                } else if (file.getName().endsWith(".zip") || file.getName().endsWith(".rar") || file.getName().endsWith(".7z")) {
                    // Nếu còn tệp nén, tiếp tục giải nén tại vị trí hiện tại của tệp nén
                    File extractedFolder = extractArchive(file);
                    if (extractedFolder != null) {
                        return processExtractedFolder(extractedFolder);  // Tiếp tục xử lý thư mục vừa giải nén
                    }
                }
            }
        }

        return null;  // Không tìm thấy tệp .sln
    }
}
