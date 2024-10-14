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

    public List<String> processFileSubmission(MultipartFile file, Long examPaperId) throws IOException {
        List<String> unmatchedStudents = Collections.synchronizedList(new ArrayList<>());

        // Bước 1: Giải nén file
        String rootFolder = fileExtractionService.extract7zWithApacheCommons(file, uploadDir);

        // Bước 2: Truy cập thư mục chứa bài nộp
        String mainSourcePath = uploadDir + "\\" + rootFolder;
        File studentSolutionFolder = new File(mainSourcePath + "\\StudentSolution");

        // Lưu main source path vào source
        Source source = sourceService.saveMainSource(mainSourcePath, examPaperId);

        // Kiểm tra nếu thư mục StudentSolution tồn tại và là một thư mục
        if (studentSolutionFolder.exists() && studentSolutionFolder.isDirectory()) {
            // Lấy danh sách tất cả các thư mục con bên trong StudentSolution (ví dụ: thư mục "1")
            File[] subFolders = studentSolutionFolder.listFiles(File::isDirectory);

            // Kiểm tra nếu có thư mục con nào bên trong
            if (subFolders != null && subFolders.length > 0) {
                // Lấy thư mục con đầu tiên (ví dụ: thư mục "1")
                File actualStudentFolder = subFolders[0]; // Thư mục "1" có thể là subFolders[0]

                // Kiểm tra nếu actualStudentFolder tồn tại và là một thư mục
                if (actualStudentFolder.exists() && actualStudentFolder.isDirectory()) {
                    // Lấy danh sách các thư mục sinh viên bên trong actualStudentFolder
                    File[] studentFolders = actualStudentFolder.listFiles(File::isDirectory);

                    // Kiểm tra nếu có thư mục sinh viên nào bên trong
                    if (studentFolders != null && studentFolders.length > 0) {
                        // Xử lý song song các thư mục sinh viên
                        Arrays.stream(studentFolders).parallel().forEach(studentFolder -> {
                            try {
                                String studentCode = extractStudentCode(studentFolder.getName());
                                logger.info("Processing student folder: {}", studentCode);

                                Optional<Student> student = studentRepository.findByStudentCode(studentCode);

                                if (student.isPresent()) {
                                    // Sinh viên tồn tại trong cơ sở dữ liệu
                                    sourceDetailService.saveStudentSubmission(studentFolder, student.get(), source);
                                    logger.info("Submission saved for student: {}", studentCode);
                                } else {
                                    // Không tìm thấy sinh viên trong cơ sở dữ liệu
                                    unmatchedStudents.add(studentCode);
                                    logger.warn("Unmatched student: {}", studentCode);
                                }
                            } catch (Exception e) {
                                logger.error("Error processing folder {}: {}", studentFolder.getName(), e.getMessage());
                            }
                        });
                    } else {
                        logger.error("No student folders found inside: {}", actualStudentFolder.getPath());
                        throw new IOException("No student folders found inside the actual student folder");
                    }
                } else {
                    logger.error("Actual student folder does not exist or is not a directory: {}", actualStudentFolder.getPath());
                    throw new IOException("Actual student folder is missing or invalid");
                }
            } else {
                logger.error("No subfolders found inside StudentSolution.");
                throw new IOException("No subfolders found inside StudentSolution");
            }
        } else {
            logger.error("StudentSolution folder does not exist or is not a directory: {}", studentSolutionFolder.getPath());
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
    
}
