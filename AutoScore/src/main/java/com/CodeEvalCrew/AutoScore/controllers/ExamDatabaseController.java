package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.services.examdatabase_service.ExamDatabaseService;

@RestController
@RequestMapping("/api/database")
public class ExamDatabaseController {

    @Autowired
    private ExamDatabaseService examDatabaseService;

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') or hasAuthority('CREATE_EXAM_DATABASE')")
    @PostMapping("/import")
    public ResponseEntity<String> importSqlFile(@RequestParam("file") MultipartFile sqlFile,
                                                @RequestParam("image") MultipartFile imageFile) {
        try {
            String result = examDatabaseService.importSqlFile(sqlFile, imageFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
