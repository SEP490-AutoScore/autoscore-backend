package com.CodeEvalCrew.AutoScore.services.postman_for_grading_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingCreateDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PostmanForGradingUpdateGetDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingGetDTO;

public interface IPostmanForGradingService {
      List<PostmanForGradingDTO> getPostmanForGradingByExamPaperId(Long examPaperId);

      // void updatePostmanForGradingList(List<PostmanForGradingDTO> postmanForGradingDTOs);

      String generatePostmanCollection(Long gherkinScenarioId);

      String generatePostmanCollectionMore(Long gherkinScenarioId);

      // Hàm merge các file Postman collection của cùng 1 examPaperId
      String mergePostmanCollections(Long examPaperId);

      String updatePostmanForGrading(Long examPaperId, List<PostmanForGradingUpdateDTO> updateDTOs);

      String deletePostmanForGrading(Long postmanForGradingId);

      PostmanForGradingGetDTO getPostmanForGradingById(Long postmanForGradingId);

      // PostmanForGradingGetDTO updatePostmanForGrading(Long postmanForGradingId, PostmanForGradingUpdateGetDTO updateDTO);
      
      PostmanForGradingGetDTO createPostmanForGrading(PostmanForGradingCreateDTO createDTO);
     
}
