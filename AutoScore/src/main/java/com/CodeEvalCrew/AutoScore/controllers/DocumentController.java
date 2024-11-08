package com.CodeEvalCrew.AutoScore.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.services.document_service.DocumentService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

public class DocumentController {
    
    @Autowired
    private DocumentService documentService;

    @PostMapping("/generate-word")
    public ResponseEntity<byte[]> generateWord(@RequestBody Long examPaperId) {
        // Dotenv dotenv = Dotenv.load();
        // String path = dotenv.get("PATH");
        try {
            // Merge data into the Word template
            documentService.mergeDataToWord(examPaperId);
            
            String outputPath = "C:\\Project\\SEP490\\output.docx";
            
            // Read the output file and return it as a downloadable file
            File file = new File(outputPath);
            byte[] documentContent = new FileInputStream(file).readAllBytes();
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
}
