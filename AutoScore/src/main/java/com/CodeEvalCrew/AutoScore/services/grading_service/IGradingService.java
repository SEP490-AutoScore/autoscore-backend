package com.CodeEvalCrew.AutoScore.services.grading_service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Grading.GradingRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Grading.GradingRequestForExam;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GradingProcessView;

public interface IGradingService {

    void startingGradingProcess(GradingRequest request) throws Exception, NotFoundException;

    GradingProcessView loadingGradingProgress(Long examPaperId);

    void startingGradingProcessForExamPaper(GradingRequestForExam request) throws Exception,NotFoundException;
    
}
