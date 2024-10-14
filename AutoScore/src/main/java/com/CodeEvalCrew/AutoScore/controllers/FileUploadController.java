package com.CodeEvalCrew.AutoScore.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import com.CodeEvalCrew.AutoScore.services.student_service.StudentSubmissionService;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private StudentSubmissionService studentSubmissionService;

    @PreAuthorize("hasAnyRole('ADMIN', 'EXAMINER') or hasAuthority('UPLOAD_FILE')")
    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @Operation(
            summary = "Tải lên file source code của sinh viên",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = {
                        @Content(mediaType = "multipart/form-data")
                    }
            )
    )
     public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("exam_paper_id") Long examPaperId) throws IOException {
        List<String> unmatchedStudents;

        try {
            // Điều hướng sang service xử lý file và submission của sinh viên
            unmatchedStudents = studentSubmissionService.processFileSubmission(file, examPaperId);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during file upload: " + e.getMessage());
        }

        if (unmatchedStudents.isEmpty()) {
            return ResponseEntity.ok("File uploaded successfully with no unmatched students.");
        } else {
            return ResponseEntity.ok("File uploaded successfully, but unmatched students found: " + unmatchedStudents);
        }
    }
}
