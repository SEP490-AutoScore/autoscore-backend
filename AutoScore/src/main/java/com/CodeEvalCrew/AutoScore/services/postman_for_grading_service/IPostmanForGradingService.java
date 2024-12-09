package com.CodeEvalCrew.AutoScore.services.postman_for_grading_service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.GradingRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingGetbyIdDTO;

public interface IPostmanForGradingService {
      List<PostmanForGradingDTO> getPostmanForGradingByExamPaperId(Long examPaperId);

      // void updatePostmanForGradingList(List<PostmanForGradingDTO>
      // postmanForGradingDTOs);

      ResponseEntity<?> generatePostmanCollection(Long gherkinScenarioId);

      ResponseEntity<?> generatePostmanCollectionMore(Long gherkinScenarioId);

      // Hàm merge các file Postman collection của cùng 1 examPaperId
      String mergePostmanCollections(Long examPaperId);

      String updatePostmanForGrading(Long examPaperId, List<PostmanForGradingUpdateDTO> updateDTOs);

      String deletePostmanForGrading(List<Long> postmanForGradingIds, Long examPaperId);

      ResponseEntity<PostmanForGradingGetbyIdDTO> getPostmanForGradingById(Long id);

      String updateExamQuestionId(Long postmanForGradingId, Long examQuestionId);

      ResponseEntity<?> calculateScores(List<GradingRequestDTO> requests, Long examPaperId, Long examQuestionId);

}
