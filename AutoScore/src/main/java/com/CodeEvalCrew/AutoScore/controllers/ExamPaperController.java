package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperToExamRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperFilePostmanResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperView;
import com.CodeEvalCrew.AutoScore.services.exam_paper_service.IExamPaperService;

@RestController
@RequestMapping("api/exam-paper")
public class ExamPaperController {

    @Autowired
    private final IExamPaperService examPaperService;

    public ExamPaperController(IExamPaperService examPaperService) {
        this.examPaperService = examPaperService;
    }

    @PreAuthorize("hasAnyAuthority('VIEW_EXAM_PAPER', 'ALL_ACCESS')")
    @GetMapping("{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ExamPaperView result;
        try {

            result = examPaperService.getById(id);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('CREATE_EXAM_PAPER','ALL_ACCESS')")
    @PostMapping("")
    public ResponseEntity<?> createNewExamPaper(@RequestBody ExamPaperCreateRequest request) {
        ExamPaperView result;
        try {

            result = examPaperService.createNewExamPaper(request);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('CREATE_EXAM_PAPER','ALL_ACCESS')")
    @PostMapping("/new")
    public ResponseEntity<?> createNewExamPaperNotUsed(@RequestBody ExamPaperCreateRequest request) {
        ExamPaperView result;
        try {

            result = examPaperService.createNewExamPaperNotUsed(request);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_EXAM_PAPER', 'ALL_ACCESS')")
    @PutMapping("{id}")
    public ResponseEntity<?> updateExamPaper(@PathVariable Long id, @RequestBody ExamPaperCreateRequest request) {
        ExamPaperView result;
        try {

            result = examPaperService.updateExamPaper(id, request);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_EXAM_PAPER', 'ALL_ACCESS')")
    @PostMapping("list")
    public ResponseEntity<?> getList(@RequestBody ExamPaperViewRequest request) {
        List<ExamPaperView> result;
        try {

            result = examPaperService.getList(request);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('DELETE_EXAM_PAPER','ALL_ACCESS')")
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteExamPaper(@PathVariable Long id) {
        ExamPaperView result;
        try {

            result = examPaperService.deleteExamPaper(id);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('IMPORT_POSTMAN', 'ALL_ACCESS')")
    @PostMapping("/import-postman-collections")
    public ResponseEntity<?> importPostmanCollections(
            @RequestParam("examPaperId") Long examPaperId,
            @RequestParam("files") List<MultipartFile> files) throws Exception {
        try {
            examPaperService.importPostmanCollections(examPaperId, files);
            return new ResponseEntity<>("Files imported and validated successfully.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to import files: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyAuthority('EXPORT_POSTMAN', 'ALL_ACCESS')")
    @GetMapping("/export-postman/{examPaperId}")
    public ResponseEntity<byte[]> exportPostmanCollection(@PathVariable Long examPaperId) {
        try {
            byte[] fileContent = examPaperService.exportPostmanCollection(examPaperId);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=postman_collection.json");
            headers.add("Content-Type", "application/json");

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (Exception e) {

            byte[] errorMessage = ("Failed to export Postman Collection: " + e.getMessage()).getBytes();
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_EXAM_PAPER', 'ALL_ACCESS')")
    @GetMapping("/all")
    public ResponseEntity<?> getAllExamPaper() {
        List<ExamPaperView> result;
        try {
            result = examPaperService.getAllExamNotUsed();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_GHERKIN_POSTMAN', 'ALL_ACCESS')")
    @GetMapping("/infoFilePostman")
    public ResponseEntity<?> getExamPaper(@RequestParam Long examPaperId) {
        try {
            ExamPaperFilePostmanResponseDTO response = examPaperService.getInfoFilePostman(examPaperId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_POSTMAN', 'ALL_ACCESS')")
    @PutMapping("/confirmFilePostman/{examPaperId}")
    public ResponseEntity<?> confirmFilePostman(@PathVariable Long examPaperId) {
        try {
            String result = examPaperService.confirmFilePostman(examPaperId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('UPDATE_EXAM_PAPER', 'ALL_ACCESS')")
    @PutMapping("/exam-paper")
    public ResponseEntity<?> updateExamPaperToAnExam(@RequestBody ExamPaperToExamRequest examPaperId) {
        try {
            examPaperService.updateExamPaperToAnExam(examPaperId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PreAuthorize("hasAnyAuthority('ALL_ACCESS')")
    @PutMapping("/exam-paper/{examPaperId}")
    public ResponseEntity<?> usedExamPaperInExam(@PathVariable Long examPaperId) {
        try {
            examPaperService.updateIsused(examPaperId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('CREATE_EXAM_PAPER', 'ALL_ACCESS')")
    @PutMapping("/exam-paper/complete/{examPaperId}")
    public ResponseEntity<?> completeExamPaper(@PathVariable Long examPaperId) {
        try {
            examPaperService.completeExamPaper(examPaperId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
