package com.CodeEvalCrew.AutoScore.services.file_service;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.springframework.stereotype.Service;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.*;
import java.nio.file.Files;

@Service
public class FileExtractionService {

    // Giải nén file 7z bằng Apache Commons Compress
    public void extract7zWithApacheCommons(File archive, File outputDir) throws IOException {
        try (SevenZFile sevenZFile = new SevenZFile(archive)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File outFile = new File(outputDir, entry.getName());
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
}
