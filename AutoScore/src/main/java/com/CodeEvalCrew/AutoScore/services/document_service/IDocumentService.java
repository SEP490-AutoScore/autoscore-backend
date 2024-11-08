package com.CodeEvalCrew.AutoScore.services.document_service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;

public interface IDocumentService {
    void mergeDataToWord(Long examPaperId) throws Exception, NotFoundException;
}
