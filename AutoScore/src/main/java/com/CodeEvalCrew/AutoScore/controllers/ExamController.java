package com.CodeEvalCrew.AutoScore.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.docx4j.model.fields.merge.DataFieldName;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamViewRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;
import com.CodeEvalCrew.AutoScore.services.exam_service.IExamService;

@RestController
@RequestMapping("api/exam/")
public class ExamController {

    private final IExamService examService;

    public ExamController(IExamService examService) {
        this.examService = examService;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @GetMapping("{id}")
    public ResponseEntity<?> getExamById(@PathVariable long id) {
        ExamViewResponseDTO result;
        try {
            result = examService.getExamById(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @PostMapping("")
    public ResponseEntity<?> getExam(@RequestBody ExamViewRequestDTO request) {
        List<ExamViewResponseDTO> result;
        try {
            // call service to get result
            result = examService.GetExam(request);
        } catch (NoSuchElementException nsee) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            // return notfound
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @PutMapping("")
    public ResponseEntity<?> creatNewExam(@RequestBody ExamCreateRequestDTO entity) {
        try {
            //call service for create new exam
            ExamViewResponseDTO result = examService.createNewExam(entity);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EXAMINER','HEAD_OF_DEPARTMENT') and hasAuthority('VIEW_EXAM')")
    @PutMapping("/{id}")
    public ResponseEntity<?> putMethodName(@PathVariable String id, @RequestBody ExamCreateRequestDTO request) {
        try {
            //call service for update exam
            ExamViewResponseDTO result = examService.updateExam(request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException nfe) {
            return new ResponseEntity<>(nfe.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergeData(@RequestBody Map<String, Object> data) {
        try {
            String templatePath = "AutoScore\\src\\main\\resources\\Template.docx"; // Path to your template

            // Merge the data into the template
            byte[] mergedDocument = examService.mergeDataIntoTemplate(templatePath, data);

            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "merged_document.docx");

            // Return the document as a downloadable file
            return new ResponseEntity<>(mergedDocument, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/merge-word")
    public String mergeWord(@RequestParam String examCode, @RequestParam String examPaperCode, @RequestParam String semester) {
        try {
            String templatePath = "C:\\Project\\SEP490\\tp.docx";
            // Prepare the merge data
            Map<DataFieldName, String> data = new HashMap<>();
            data.put(new DataFieldName("ExamCode"), examCode);
            data.put(new DataFieldName("ExamPaperCode"), examPaperCode);
            data.put(new DataFieldName("Semester"), semester);

            // Merge data into the Word template
            examService.mergeDataIntoWord(templatePath, "C:\\Project\\SEP490\\output.docx", data);

            return "Word document merged successfully!";
        } catch (Exception e) {
            return "Error merging Word document: " + e.getMessage();
        }
    }

    @GetMapping("/generate-word")
    public ResponseEntity<byte[]> generateWord() throws IOException, InvalidFormatException {

        try {
            // Define the path of the template and output file
            String templatePath = "C:\\Project\\SEP490\\tp.docx";
            String outputPath = "C:\\Project\\SEP490\\output.docx";

            // Create a map of data to be merged into the document
            Map<String, String> data = new HashMap<>();
            // data.put("ExamCode", name);
            // data.put("ExamPaperCode", date);

            // Merge data into the Word template
            examService.mergeDataToWord(templatePath, outputPath, data);

            // Read the output file and return it as a downloadable file
            File file = new File(outputPath);
            byte[] documentContent = new FileInputStream(file).readAllBytes();
            // Return the file as a response
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged_word.docx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(documentContent);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }


}
