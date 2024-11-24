package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentErrorResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.services.student_error_service.IStudentErrorService;
import com.CodeEvalCrew.AutoScore.services.student_error_service.StudentErrorService;
import com.CodeEvalCrew.AutoScore.services.student_service.ExcelService;
import com.CodeEvalCrew.AutoScore.services.student_service.IStudentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final ExcelService excelService;
    private final IStudentService studentService;
    private final IStudentErrorService studentErrorService;

    public StudentController(ExcelService excelService, IStudentService studentService, IStudentErrorService studentErrorService) {
        this.excelService = excelService;
        this.studentService = studentService;
        this.studentErrorService = studentErrorService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EXAMINER')")
    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @Operation(
            summary = "Tải lên file Excel chứa thông tin sinh viên",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = {
                        @Content(mediaType = "multipart/form-data")
                    }
            )
    )
    public ResponseEntity<?> importExcelFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("examId") Long examId,
            @RequestParam("organizationId") Long organizationId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
            }
            List<Student> students = excelService.importExcelFile(file, examId, organizationId);
            studentService.saveStudents(students);
            return ResponseEntity.status(HttpStatus.OK).body("File imported and students saved successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to import file");
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'EXAMINER') or hasAuthority('VIEW_SCORE')")
    @PostMapping("/student-error")
    public ResponseEntity<List<StudentErrorResponseDTO>> getStudentError(@RequestParam Long sourceid) {
        try {
            List<StudentErrorResponseDTO> studentErrorResponseDTOs = studentErrorService.getStudentErrorBySourceId(sourceid);
            return ResponseEntity.ok(studentErrorResponseDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
