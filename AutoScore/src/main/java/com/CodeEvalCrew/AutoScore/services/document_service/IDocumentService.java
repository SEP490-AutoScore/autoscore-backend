package com.CodeEvalCrew.AutoScore.services.document_service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;

public interface IDocumentService {
    byte[] mergeDataToWord(Long examPaperId) throws Exception, NotFoundException;
}
