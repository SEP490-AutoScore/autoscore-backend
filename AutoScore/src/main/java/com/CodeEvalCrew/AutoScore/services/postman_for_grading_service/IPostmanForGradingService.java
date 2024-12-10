package com.CodeEvalCrew.AutoScore.services.postman_for_grading_service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingGetbyIdDTO;

public interface IPostmanForGradingService {
      List<PostmanForGradingDTO> getPostmanForGradingByExamPaperId(Long examPaperId);

      ResponseEntity<?> generatePostmanCollection(Long gherkinScenarioId);

      ResponseEntity<?> generatePostmanCollectionMore(Long postmanForGradingId);

      String mergePostmanCollections(Long examPaperId);

      String updatePostmanForGrading(Long examPaperId, List<PostmanForGradingUpdateDTO> updateDTOs);

      String deletePostmanForGrading(List<Long> postmanForGradingIds, Long examPaperId);

      ResponseEntity<PostmanForGradingGetbyIdDTO> getPostmanForGradingById(Long id);

      String updateExamQuestionId(Long postmanForGradingId, Long examQuestionId);

      void calculateScores(Long examPaperId) throws Exception;

}
