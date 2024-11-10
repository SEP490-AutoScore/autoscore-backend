package com.CodeEvalCrew.AutoScore.controllers;

import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.services.document_service.IDocumentService;

@RestController
@RequestMapping("api/document/")
public class DocumentController {
    
    @Autowired
    private IDocumentService documentService;

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
}
