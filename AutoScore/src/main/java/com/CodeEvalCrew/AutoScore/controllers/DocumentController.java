package com.CodeEvalCrew.AutoScore.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.services.document_service.IDocumentService;

@RestController
@RequestMapping("api/document/")
public class DocumentController {
    
    @Autowired
    private IDocumentService documentService;

    @PreAuthorize("hasAnyAuthority('ALL_ACCESS')")
    @GetMapping("/generate-word")
    public ResponseEntity<byte[]> generateWord(@RequestParam Long examPaperId) {
        // Dotenv dotenv = Dotenv.load();
        // String path = dotenv.get("PATH");
        try {
            // Merge data into the Word template
            byte[] documentContent = documentService.mergeDataToWord(examPaperId);
            // Return the file as a response
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=examPaper.docx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(documentContent);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InvalidFormatException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }   catch (NotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('ALL_ACCESS')")
    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    public ResponseEntity<?> importExamPaper(@RequestParam("file") MultipartFile file, @RequestParam("examPaperId") Long examPaperId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Kiểm tra file
            if (file.isEmpty()) {
                response.put("message", "File is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Kiểm tra định dạng tệp
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.matches(".*\\.(doc|docx)$")) {
                response.put("message", "Only .doc or .docx files are allowed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            documentService.importExamPaper(examPaperId, file);

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch ( NotFoundException e) {
            response.put("message", "exam paper not found");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch ( Exception e) {
            response.put("message", "Error reading file");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //  @PostMapping
    // public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
    //     Map<String, Object> response = new HashMap<>();

    //     try {
    //         // Kiểm tra file
    //         if (file.isEmpty()) {
    //             response.put("message", "File is empty");
    //             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    //         }

    //         // Kiểm tra định dạng tệp
    //         String fileName = file.getOriginalFilename();
    //         if (fileName == null || !fileName.matches(".*\\.(doc|docx)$")) {
    //             response.put("message", "Only .doc or .docx files are allowed");
    //             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    //         }

    //         // Lưu tệp
    //         Path uploadPath = Paths.get(UPLOAD_DIR);
    //         if (!Files.exists(uploadPath)) {
    //             Files.createDirectories(uploadPath); // Tạo thư mục nếu chưa tồn tại
    //         }
    //         Path filePath = uploadPath.resolve(file.getOriginalFilename());
    //         file.transferTo(filePath.toFile());

    //         response.put("message", "File uploaded successfully");
    //         response.put("filePath", filePath.toString());
    //         return ResponseEntity.status(HttpStatus.OK).body(response);

    //     } catch (IOException e) {
    //         response.put("message", "Error saving file");
    //         response.put("error", e.getMessage());
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    //     }
    // }
}
