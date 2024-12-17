package com.CodeEvalCrew.AutoScore.services.exam_paper_service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperToExamRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperFilePostmanResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperView;

public interface IExamPaperService {

    ExamPaperView getById(Long id) throws NotFoundException;

    List<ExamPaperView> getList(ExamPaperViewRequest request) throws NotFoundException, NoSuchElementException;

    ExamPaperView createNewExamPaper(ExamPaperCreateRequest request) throws NotFoundException;

    ExamPaperView updateExamPaper(Long id, ExamPaperCreateRequest request) throws NotFoundException;

    ExamPaperView deleteExamPaper(Long id) throws NotFoundException;

    void importPostmanCollections(Long examPaperId, List<MultipartFile> files) throws Exception;

    byte[] exportPostmanCollection(Long examPaperId) throws Exception;

    List<ExamPaperView> getAllExamNotUsed() throws NotFoundException, Exception;

    ExamPaperView createNewExamPaperNotUsed(ExamPaperCreateRequest request) throws NotFoundException;

    ExamPaperFilePostmanResponseDTO getInfoFilePostman(Long examPaperId) throws NotFoundException;

    String confirmFilePostman(Long examPaperId);

    void updateExamPaperToAnExam(ExamPaperToExamRequest examPaperId) throws NotFoundException, Exception;

    void updateIsused(Long examPaperId) throws NotFoundException, Exception;

    void completeExamPaper(Long examPaperId) throws NotFoundException, Exception;

}
