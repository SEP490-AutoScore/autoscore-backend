package com.CodeEvalCrew.AutoScore.controllers;

import com.CodeEvalCrew.AutoScore.services.file_service.FileExtractionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${upload.folder}")
    private String uploadFolder;

    @Autowired
    private FileExtractionService fileExtractionService;

    @PreAuthorize("hasAnyRole('ADMIN', 'EXAMINER') or hasAuthority('UPLOAD_FILE')")
    @PostMapping("/file")
    @Operation(
            summary = "Tải lên file source code của sinh viên",
            requestBody = @RequestBody(
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "object", format = "binary", requiredProperties = {"file"})))
    )
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "Please upload a file!";
        }   

        // Set the desired path to store the uploaded file
        Path uploadPath = Path.of(uploadFolder, file.getOriginalFilename());
        file.transferTo(uploadPath.toFile()); // Save the file

        // Ensure the output directory exists
        File outputDir = new File(uploadFolder);
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Create if doesn't exist
        }

        try {
            // Extract the file based on its type
            String fileName = file.getOriginalFilename();
            if (fileName != null) {
                if (fileName.endsWith(".7z")) {
                    fileExtractionService.extract7zWithApacheCommons(uploadPath.toFile(), outputDir); // Extract 7z
                } else if (fileName.endsWith(".zip")) {
                    fileExtractionService.extractZipWithZip4j(uploadPath.toFile(), outputDir); // Extract ZIP
                } else {
                    fileExtractionService.extractWithCommonsCompress(uploadPath.toFile(), outputDir); // Other formats
                }
            }
        } finally {
            Files.deleteIfExists(uploadPath); // Clean up uploaded file
        }

        return "File uploaded and extracted successfully!";
    }
}
