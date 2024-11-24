package com.CodeEvalCrew.AutoScore.services.grading_service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Grading.GradingRequest;

public interface IGradingService {

    void startingGradingProcess(GradingRequest request) throws Exception;
    
}
