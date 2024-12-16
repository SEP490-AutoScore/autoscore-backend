package com.CodeEvalCrew.AutoScore.services.document_service;

import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;

public interface IDocumentService {
    byte[] mergeDataToWord(Long examPaperId) throws Exception, NotFoundException;

    void importExamPaper(Long examPaperId, MultipartFile file) throws Exception, NotFoundException;
}
