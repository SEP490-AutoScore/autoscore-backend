package com.CodeEvalCrew.AutoScore.services.file_service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.utils.Util;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@Service
public class FileExtractionService {
    private static final Logger logger = LoggerFactory.getLogger(FileExtractionService.class);

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
            byte[] buffer = new byte[10 * 1024 * 1024]; // Tăng kích thước bộ đệm lên 10MB
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
    public void extractZipWithZip4j(File archive, File outputDir) throws IOException {
        try {
            try (ZipFile zipFile = new ZipFile(archive)) {
                zipFile.extractAll(outputDir.getAbsolutePath());
            }
        } catch (ZipException e) {
            throw new IOException("Lỗi khi giải nén file ZIP: " + archive.getAbsolutePath(), e);
        }
    }

    // Giải nén các định dạng khác như tar, tgz, gz
    public void extractWithCommonsCompress(File archive, File outputDir) throws IOException {
        try (InputStream fi = new FileInputStream(archive); BufferedInputStream bi = new BufferedInputStream(fi); 
        ArchiveInputStream<ArchiveEntry> i = new ArchiveStreamFactory().createArchiveInputStream(bi)) {
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
            throw new IOException("Error extracting archive: " + e.getMessage(), e);
        }
    }

    // Giải nên file 7z
    public void extract7zFile(File archive, File outputDir) throws IOException {
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
            throw new IOException("Failed to extract 7z file: " + e.getMessage(), e);
        }
    }
}
