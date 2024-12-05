package com.CodeEvalCrew.AutoScore.services.score_service;

import java.io.IOException;
import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.CodePlagiarismResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreDetailsResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreOverViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentScoreDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.TopStudentDTO;

import jakarta.servlet.http.HttpServletResponse;

public interface IScoreService {

    List<ScoreResponseDTO> getScoresByExamPaperId(Long examPaperId);

    void exportScoresToExcel(HttpServletResponse response, List<ScoreResponseDTO> scores) throws IOException;

    List<ScoreOverViewResponseDTO> getScoreOverView();

    List<ScoreDetailsResponseDTO> getScoreDetailsByScoreId(Long scoreId);

    List<CodePlagiarismResponseDTO> getCodePlagiarismByScoreId(Long scoreId);

    int getTotalStudentsByExamPaperId (Long examPaperId);

    int getTotalStudentsWithZeroScore(Long examPaperId);

    int getTotalStudentsWithScoreGreaterThanZero(Long examPaperId);

    List<StudentScoreDTO> getStudentScoresByExamPaperId(Long examPaperId);

      List<TopStudentDTO> getTopStudents();
}
