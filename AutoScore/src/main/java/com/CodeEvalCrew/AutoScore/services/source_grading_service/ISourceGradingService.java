package com.CodeEvalCrew.AutoScore.services.source_grading_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceView;

public interface ISourceGradingService {

    List<SourceView> getAllSource();

}
