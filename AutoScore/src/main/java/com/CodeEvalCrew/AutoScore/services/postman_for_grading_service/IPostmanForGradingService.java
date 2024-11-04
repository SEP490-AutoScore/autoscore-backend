package com.CodeEvalCrew.AutoScore.services.postman_for_grading_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;

public interface  IPostmanForGradingService {
      List<PostmanForGradingDTO> getPostmanForGradingByExamPaperId(Long examPaperId);
      void updatePostmanForGradingList(List<PostmanForGradingDTO> postmanForGradingDTOs);
}
