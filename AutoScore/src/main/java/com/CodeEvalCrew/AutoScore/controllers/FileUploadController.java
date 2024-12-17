package com.CodeEvalCrew.AutoScore.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.CodeEvalCrew.AutoScore.services.student_service.FileProcessingProgressService;
import com.CodeEvalCrew.AutoScore.services.student_service.StudentSubmissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private StudentSubmissionService studentSubmissionService;

    @Autowired
    private FileProcessingProgressService progressService;

    @PreAuthorize("hasAnyAuthority('UPLOAD_FILE', 'ALL_ACCESS')")
    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("examId") Long examId) {
        List<String> unmatchedStudents;

        try {
            unmatchedStudents = studentSubmissionService.processFileSubmission(file, examId);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during file upload or extraction: " + e.getMessage());
        }

        if (unmatchedStudents.isEmpty()) {
            return ResponseEntity.ok("File uploaded successfully with no unmatched students.");
        } else {
            return ResponseEntity.ok("File uploaded successfully, but errors found: " + unmatchedStudents);
        }
    }

    @GetMapping(value = "/progress/{clientId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable String clientId) {
        SseEmitter emitter = new SseEmitter(900_000L);
        progressService.registerEmitter(clientId, emitter);
        return emitter;
    }
}
