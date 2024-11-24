package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.services.student_service.ExcelService;
import com.CodeEvalCrew.AutoScore.services.student_service.IStudentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final ExcelService excelService;
    private final IStudentService studentService;

    public StudentController(ExcelService excelService, IStudentService studentService) {
        this.excelService = excelService;
        this.studentService = studentService;
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

    @GetMapping("")
    public ResponseEntity<?> getAllStudentOfSource(@RequestParam Long sourceId) {
        List<StudentDTO> result;
        try{
            result = studentService.getAllStudentOfSource(sourceId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}