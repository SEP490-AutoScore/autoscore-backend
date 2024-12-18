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
import java.util.HashSet;
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
    public String extract7zWithSelectiveDelete(MultipartFile file, String uploadDir) throws IOException {
        File tempFile = new File(uploadDir, file.getOriginalFilename());
        file.transferTo(tempFile);

        // Lấy campus
        String campus = Util.getCampus();
        String uploadDirect = uploadDir;
        if (campus != null) {
            uploadDirect = uploadDir + File.separator + campus;
        }

        Set<String> examPaperCodes = new HashSet<>(); // Lưu các ExamPaperCode từ file nén
        Set<String> parentFolders = new HashSet<>();

        // Bước 1: Duyệt file nén và lấy các ExamPaperCode
        try (SevenZFile sevenZFile = new SevenZFile(tempFile)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    String[] parts = entry.getName().split("/");
                    if (parts.length > 1) {
                        parentFolders.add(parts[0].trim() + File.separator);
                        examPaperCodes.add(parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Error reading 7z file " + tempFile.getAbsolutePath());
        }

        // Debug: In ra danh sách ExamPaperCode
        System.out.println("ExamPaperCodes: " + examPaperCodes);

        // Bước 2: Xóa các folder ExamPaperCode tương ứng trên hệ thống
        for (String examCode : examPaperCodes) {
            File existingFolder = new File(uploadDirect, examCode);
            if (existingFolder.exists() && existingFolder.isDirectory()) {
                deleteFolderContents(existingFolder); // Xóa nội dung folder
                if (!existingFolder.delete()) {
                    System.err.println("Failed to delete folder " + existingFolder.getAbsolutePath());
                }
            } else {
                System.out.println("Folder does not exist " + existingFolder.getAbsolutePath());
            }
        }

        // Bước 3: Giải nén file nén 7z vào hệ thống
        try (SevenZFile sevenZFile = new SevenZFile(tempFile)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                File outFile = new File(uploadDirect, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(outFile.toPath());
                } else {
                    Files.createDirectories(outFile.getParentFile().toPath());
                    try (FileOutputStream out = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = sevenZFile.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Error extracting 7z file " + tempFile.getAbsolutePath());
        } finally {
            tempFile.delete(); // Xóa file tạm
        }

        return uploadDirect + File.separator + String.join(File.separator, parentFolders);
    }

    // Hàm xóa nội dung trong thư mục
    private void deleteFolderContents(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolderContents(file);
                    }
                    if (!file.delete()) {
                        System.err.println("Failed to delete file " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    // Giải nén đệ quy và tìm tệp .sln
    public File processExtractedFolder(File folder, Source source, Student student) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                File slnFile = processExtractedFolder(file, source, student);
                if (slnFile != null) {
                    return slnFile; // Trả về thư mục chứa file .sln nếu tìm thấy

                }
            } else if (file.getName().endsWith(".sln")) {
                return folder; // Trả về thư mục chứa file .sln
            } else if (file.getName().matches(".*\\.(zip|rar|7z|tar|gz)$")) {
                // Giải nén file nén đệ quy
                extractArchive(file, folder, source, student);
                if (!file.delete()) {
                    logger.warn("Failed to delete archive ", file.getPath());
                }
                return processExtractedFolder(folder, source, student); // Tiếp tục xử lý thư mục sau khi giải nén
            }
        }
        return null;
    }

    public File extractAllNestedArchives(File archive, Source source, Student student) throws IOException {
        File outputDir = new File(archive.getParentFile(), archive.getName() + "_extracted");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        extractArchive(archive, outputDir, source, student);

        return outputDir;
    }

    public File findSlnFile(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File slnFile = findSlnFile(file);
                    if (slnFile != null) {
                        return slnFile;
                    }
                } else if (file.getName().endsWith(".sln")) {
                    return file;
                }
            }
        }
        return null;
    }

    // Hàm giải nén file tổng quát
    public void extractArchive(File archive, File outputDir, Source source, Student student) throws IOException {
        if (archive.getName().endsWith(".zip")) {
            extractZipWithZip4j(archive, outputDir, source, student);
        } else if (archive.getName().endsWith(".rar")) {
            extractRarWith7ZipCommand(archive, outputDir, source, student);
        } else if (archive.getName().endsWith(".7z")) {
            extract7zFile(archive, outputDir, source, student);
        } else {
            extractWithCommonsCompress(archive, outputDir, source, student);
        }
    }

    // Giải nén file ZIP bằng Zip4j
    public void extractZipWithZip4j(File archive, File outputDir, Source source, Student student) throws IOException {
        try {
            try (ZipFile zipFile = new ZipFile(archive)) {
                zipFile.extractAll(outputDir.getAbsolutePath());
            }
        } catch (ZipException e) {
            studentErrorService.saveStudentError(source, student, "Failed to extract ZIP for student code " + student.getStudentCode());
            throw new IOException("Failed to extract ZIP file " + archive.getName());
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
            studentErrorService.saveStudentError(source, student, "Error extracting archive for student code " + student.getStudentCode());
            throw new IOException("Error extracting archive " + archive.getName());
        }
    }

    // Giải nên file 7z
    public void extract7zFile(File archive, File outputDir, Source source, Student student) throws IOException {
        // Ensure the output directory exists
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Create the output directory if it doesn't exist
        }

        try (SevenZFile sevenZFile = new SevenZFile(archive)) {
            SevenZArchiveEntry entry;
            byte[] buffer = new byte[8192]; // Buffer size for reading

            while ((entry = sevenZFile.getNextEntry()) != null) {
                // Create a file in the output directory with just the file name (no path)
                File outFile = new File(outputDir, entry.getName().substring(entry.getName().lastIndexOf(File.separator) + 1));

                if (entry.isDirectory()) {
                    // Create directories if the entry is a directory (optional)
                    outFile.mkdirs(); // This may be unnecessary if you are not extracting directories
                } else {
                    // Write the file data to the output file
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        int bytesRead;
                        while ((bytesRead = sevenZFile.read(buffer)) > 0) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        } catch (IOException e) {
            studentErrorService.saveStudentError(source, student, "Failed to extract 7z file for student code " + student.getStudentCode());
            throw new IOException("Failed to extract 7z file " + archive.getName());
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
