package com.CodeEvalCrew.AutoScore.services.score_service;

import java.io.IOException;
import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreResponseDTO;

import jakarta.servlet.http.HttpServletResponse;

public interface IScoreService {

    List<ScoreResponseDTO> getScoresByExamPaperId(Long examPaperId);

    void exportScoresToExcel(HttpServletResponse response, List<ScoreResponseDTO> scores) throws IOException;
}
