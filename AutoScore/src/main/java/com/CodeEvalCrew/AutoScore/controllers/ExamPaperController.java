package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperViewRequest;
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

    @GetMapping("{id}")
    public ResponseEntity<?> getById(@RequestParam Long id) {
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

    @PostMapping("list")
    public ResponseEntity<?> getList(@RequestBody ExamPaperViewRequest request) {
        List<ExamPaperView> result;
        try {

            result = examPaperService.getList(request);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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

    @GetMapping("/{examPaperId}/questions")
    public ResponseEntity<?> getExamQuestionIds(@PathVariable Long examPaperId) {
        try {
            List<Long> questionIds = examPaperService.getExamQuestionIdsByExamPaperId(examPaperId);
            return new ResponseEntity<>(questionIds, HttpStatus.OK);
        } catch (NotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
