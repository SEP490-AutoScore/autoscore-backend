package com.CodeEvalCrew.AutoScore.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentErrorResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.services.student_error_service.IStudentErrorService;
import com.CodeEvalCrew.AutoScore.services.student_service.ExcelService;
import com.CodeEvalCrew.AutoScore.services.student_service.IStudentService;
import com.CodeEvalCrew.AutoScore.utils.UploadProgressListener;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;


@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final ExcelService excelService;
    private final IStudentService studentService;
    private final UploadProgressListener progressListener;
    private final IStudentErrorService studentErrorService;

    public StudentController(ExcelService excelService, IStudentService studentService, UploadProgressListener progressListener, IStudentErrorService studentErrorService) {
        this.excelService = excelService;
        this.studentService = studentService;
        this.progressListener = progressListener;
        this.studentErrorService = studentErrorService;
    }

    @PreAuthorize("hasAnyAuthority('IMPORT_STUDENT', 'ALL_ACCESS')")
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
            @RequestParam("examId") Long examId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
            }
            // Cập nhật tiến trình
            progressListener.updateProgress(0, file.getSize());
            List<Student> students = excelService.importExcelFile(file, examId, progressListener);
            studentService.saveStudents(students, examId);
            return ResponseEntity.status(HttpStatus.OK).body("File imported and students saved successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to import file");
        }
    }


    // Tạo API SSE để gửi tiến trình
    @GetMapping("/upload-progress")
    public SseEmitter getUploadProgress() {
        SseEmitter emitter = new SseEmitter();
        new Thread(() -> {
            try {
                while (progressListener.getPercentComplete() <= 100) {
                    emitter.send(progressListener.getPercentComplete());
                    Thread.sleep(100);
                }
                emitter.complete();
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    @PreAuthorize("hasAnyAuthority('VIEW_STUDENT', 'ALL_ACCESS')")
    @GetMapping("/getall")
    public ResponseEntity<List<StudentResponseDTO>> getAllStudents(@RequestParam("examId") Long examId) {
        List<StudentResponseDTO> students = studentService.getAllStudents(examId);
        if (students == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(students);
    }
    @GetMapping("")
    public ResponseEntity<?> getAllStudentOfSource(@RequestParam Long examPaperId) {
        List<StudentDTO> result;
        try{
            result = studentService.getAllStudentOfSource(examPaperId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_SCORE', 'ALL_ACCESS')")
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
