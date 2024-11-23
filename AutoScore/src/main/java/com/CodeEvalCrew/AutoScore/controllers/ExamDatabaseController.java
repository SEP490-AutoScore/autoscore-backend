package com.CodeEvalCrew.AutoScore.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamDatabaseDTO;
import com.CodeEvalCrew.AutoScore.services.examdatabase_service.IExamDatabaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RequestMapping("/api/database")
public class ExamDatabaseController {

    @Autowired
    private IExamDatabaseService examDatabaseService;

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') or hasAuthority('CREATE_EXAM_DATABASE')")
    @PostMapping(value = "/import", consumes = { "multipart/form-data" })
@Operation(requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
        @Content(mediaType = "multipart/form-data")
}))
public ResponseEntity<String> importSqlFile(
        @RequestParam("file.sql") MultipartFile sqlFile,
        @RequestParam("fileimage") MultipartFile imageFile,
        @RequestParam("examPaperId") Long examPaperId,
        @RequestParam("databaseNote") String databaseNote,
        @RequestParam("databaseDescription") String databaseDescription) {
    try {
        String result = examDatabaseService.importSqlFile(sqlFile, imageFile, examPaperId, databaseNote, databaseDescription);
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
}

    // @PostMapping(value = "/import", consumes = { "multipart/form-data" })
    // @Operation(requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
    //         @Content(mediaType = "multipart/form-data")
    // }))
    // public ResponseEntity<String> importSqlFile(@RequestParam("file.sql") MultipartFile sqlFile,
    //         @RequestParam("fileimage") MultipartFile imageFile,
    //         @RequestParam("examPaperId") Long examPaperId) {
    //     try {
    //         String result = examDatabaseService.importSqlFile(sqlFile, imageFile, examPaperId);
    //         return ResponseEntity.ok(result);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Error: " + e.getMessage());
    //     }
    // }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER') or hasAuthority('UPDATE_EXAM_DATABASE')")
    @PutMapping(value = "/update", consumes = { "multipart/form-data" })
    @Operation(requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = "multipart/form-data") }))
    public ResponseEntity<String> updateSqlFile(@RequestParam("file.sql") MultipartFile sqlFile,
            @RequestParam("fileimage") MultipartFile imageFile,
            @RequestParam("examPaperId") Long examPaperId,
            @RequestParam("databaseNote") String databaseNote,
            @RequestParam("databaseDescription") String databaseDescription) {
        try {
            String result = examDatabaseService.updateSqlFile(sqlFile, imageFile, examPaperId, databaseNote, databaseDescription);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // @GetMapping("/getbyExamPaperId")
    // public ResponseEntity<ExamDatabaseDTO>
    // getExamDatabaseByExamPaperId(@RequestParam Long examPaperId) {
    // ExamDatabaseDTO result =
    // examDatabaseService.getExamDatabaseByExamPaperId(examPaperId);
    // return ResponseEntity.ok(result);
    // }
    @GetMapping("/getbyExamPaperId")
    public ResponseEntity<ExamDatabaseDTO> getExamDatabaseByExamPaperId(@RequestParam Long examPaperId) {
        Optional<ExamDatabaseDTO> result = examDatabaseService.getExamDatabaseByExamPaperId(examPaperId);

        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.noContent().build();
        }
    }

}
