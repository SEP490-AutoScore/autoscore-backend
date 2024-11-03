package com.CodeEvalCrew.AutoScore.services.file_service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.services.student_error_service.StudentErrorService;
import com.CodeEvalCrew.AutoScore.utils.Util;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@Service
public class FileExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(FileExtractionService.class);
    private final StudentErrorService studentErrorService;

    public FileExtractionService(StudentErrorService studentErrorService) {
        this.studentErrorService = studentErrorService;
    }

    @Value("${7z.path}")
    private String sevenZPath;

    // Giải nén file 7z bằng Apache Commons Compress
    public String extract7zWithApacheCommons(MultipartFile file, String uploadDir) throws IOException {
        File tempFile = new File(uploadDir, file.getOriginalFilename());
        file.transferTo(tempFile);

        // Lấy thông tin campus
        Set<Organization> organizations = Util.getOrganizations();
        String campus = null;
        if (organizations != null) {
            for (Organization organization : organizations) {
                if (organization.getType() == Organization_Enum.CAMPUS) {
                    campus = organization.getName();
                    break;
                }
            }
        }

        String rootFolder = null;
        File outputDir = new File(uploadDir, campus);
        outputDir.mkdir();

        long startTime = System.currentTimeMillis();  // Bắt đầu theo dõi thời gian

        try (SevenZFile sevenZFile = new SevenZFile(tempFile)) {
            SevenZArchiveEntry entry;
            byte[] buffer = new byte[4 * 1024 * 1024]; 
            while ((entry = sevenZFile.getNextEntry()) != null) {
                File outFile = new File(outputDir, entry.getName());

                if (entry.isDirectory()) {
                    if (rootFolder == null) {
                        rootFolder = outFile.getName();  // Lưu tên thư mục gốc
                    }
                    Files.createDirectories(outFile.toPath());
                    logger.info("Directory created: {}", outFile.getPath());
                } else {
                    Files.createDirectories(outFile.getParentFile().toPath());

                    // Ghi file
                    try (FileOutputStream out = new FileOutputStream(outFile)) {
                        int len;
                        while ((len = sevenZFile.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                    logger.info("File extracted: {}", outFile.getPath());
                }

                // Kiểm tra thời gian xử lý và log cảnh báo nếu cần
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime > 60000) {  // Quá 60 giây
                    logger.warn("Extracting file took longer than expected: {}, elapsedTime: {} ms", entry.getName(), elapsedTime);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to extract 7z file: {}", e.getMessage());
            throw new IOException("Error extracting file: " + e.getMessage());
        } finally {
            // Dọn dẹp file tạm
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }

        if (rootFolder == null) {
            logger.error("Invalid folder structure in archive");
            throw new IOException("Invalid folder structure in archive");
        }

        return campus + "\\" + rootFolder;
    }

    // Giải nén file ZIP bằng Zip4j
    public void extractZipWithZip4j(File archive, File outputDir, Source source, Student student) throws IOException {
        try {
            try (ZipFile zipFile = new ZipFile(archive)) {
                zipFile.extractAll(outputDir.getAbsolutePath());
            }
        } catch (ZipException e) {
            studentErrorService.saveStudentError(source, student, "Failed to extract ZIP for student code: " + student.getStudentCode());
            throw new IOException("Failed to extract ZIP file: " + archive.getAbsolutePath(), e);
        }
    }

    // Giải nén các định dạng khác như tar, tgz, gz
    public void extractWithCommonsCompress(File archive, File outputDir, Source source, Student student) throws IOException {
        try (InputStream fi = new FileInputStream(archive); BufferedInputStream bi = new BufferedInputStream(fi); ArchiveInputStream<ArchiveEntry> i = new ArchiveStreamFactory().createArchiveInputStream(bi)) {
            ArchiveEntry entry;
            while ((entry = i.getNextEntry()) != null) {
                File outputFile = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(outputFile.toPath());
                } else {
                    Files.createDirectories(outputFile.getParentFile().toPath());
                    try (OutputStream o = Files.newOutputStream(outputFile.toPath())) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = i.read(buffer)) > 0) {
                            o.write(buffer, 0, len);
                        }
                    }
                }
            }
        } catch (ArchiveException e) {
            // Xử lý ngoại lệ
            studentErrorService.saveStudentError(source, student, "Error extracting archive for student code: " + student.getStudentCode());
            throw new IOException("Error extracting archive: " + e.getMessage(), e);
        }
    }

    // Giải nên file 7z
    public void extract7zFile(File archive, File outputDir, Source source, Student student) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Create the output directory if it doesn't exist
        }

        try (SevenZFile sevenZFile = new SevenZFile(archive)) {
            SevenZArchiveEntry entry;
            byte[] buffer = new byte[8192]; // Adjust the buffer size if needed

            while ((entry = sevenZFile.getNextEntry()) != null) {
                File outFile = new File(outputDir, entry.getName());

                if (entry.isDirectory()) {
                    outFile.mkdirs(); // Create directories for the extracted entries
                } else {
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        int bytesRead;
                        while ((bytesRead = sevenZFile.read(buffer)) > 0) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        } catch (IOException e) {
            studentErrorService.saveStudentError(source, student, "Failed to extract 7z file for student code: " + student.getStudentCode());
            throw new IOException("Failed to extract 7z file: " + e.getMessage(), e);
        }
    }

    // Giải nén file RAR bằng 7z
    public void extractRarWith7ZipCommand(File archive, File outputDir, Source source, Student student) throws IOException {
        // logger.info("Start extracting RAR file: {}", archive.getAbsolutePath());

        if (!outputDir.exists()) {
            // logger.info("Output directory does not exist. Create directory: {}", outputDir.getAbsolutePath());
            outputDir.mkdirs(); // Tạo thư mục đầu ra nếu chưa tồn tại
        }

        // Đường dẫn đầy đủ đến 7z.exe
        String sevenZipPath = sevenZPath; // Được inject từ @Value("${7z.path}")
        File sevenZipFile = new File(sevenZipPath);
        if (!sevenZipFile.exists()) {
            throw new IOException("7z.exe not found at: " + sevenZipPath);
        }
        // logger.info("Use 7z.exe at: {}", sevenZipPath);

        // Câu lệnh hệ thống để giải nén RAR bằng 7-Zip
        String[] command = {sevenZipPath, "x", archive.getAbsolutePath(), "-o" + outputDir.getAbsolutePath()};
        // logger.info("Execute command: {}", String.join(" ", command));

        // Thực thi câu lệnh
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(false); // Không kết hợp stderr vào stdout

        Process process = processBuilder.start();

        // Xử lý stdout
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), line -> logger.info("7z stdout: {}", line));
        Thread outputThread = new Thread(outputGobbler);
        outputThread.start();

        // Xử lý stderr
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), line -> logger.error("7z stderr: {}", line));
        Thread errorThread = new Thread(errorGobbler);
        errorThread.start();

        try {
            // Đợi tiến trình kết thúc với thời gian chờ tối đa
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("RAR decompression process timed out.");
            }

            int exitCode = process.exitValue();
            // logger.info("7z process exited with code: {}", exitCode);
            if (exitCode != 0) {
                throw new IOException("Error while extracting RAR with error code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            studentErrorService.saveStudentError(source, student, "Error while processing RAR file for student code: " + student.getStudentCode());
            throw new IOException("The decompression process was interrupted.", e);
        }

        // Đảm bảo các luồng đã hoàn thành
        try {
            outputThread.join();
            errorThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            studentErrorService.saveStudentError(source, student, "Error while processing RAR file for student code: " + student.getStudentCode());
            throw new IOException("Stream processing was interrupted.", e);
        }

        // logger.info("File successfully extracted: {}", archive.getAbsolutePath());
    }

    // Định nghĩa lớp StreamGobbler
    private static class StreamGobbler implements Runnable {

        private final InputStream inputStream;
        private final Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    consumer.accept(line);
                }
            } catch (IOException e) {
                logger.error("Error in StreamGobbler: {}", e.getMessage());
            }
        }
    }
}
