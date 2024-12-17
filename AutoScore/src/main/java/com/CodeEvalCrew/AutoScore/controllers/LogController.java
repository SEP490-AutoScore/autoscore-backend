package com.CodeEvalCrew.AutoScore.controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.services.log_service.ILogService;

@RestController
@RequestMapping("api/log")
public class LogController {

    @Autowired
    private ILogService logService;

    @PreAuthorize("hasAnyAuthority('EXPORT_LOG', 'ALL_ACCESS')")
    @PostMapping("/export")
    public ResponseEntity<Resource> exportLog(@RequestParam Long examPaperId) {
        try {
            String fileName = logService.exportLogToFile(examPaperId);
    
            Path filePath = Paths.get(fileName).toAbsolutePath();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IOException("File not found or unreadable: " + fileName);
            }
    
            String contentDisposition = "attachment; filename=\"" + fileName + "\"";
            System.out.println("Content-Disposition header: " + contentDisposition);
    
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    

}
