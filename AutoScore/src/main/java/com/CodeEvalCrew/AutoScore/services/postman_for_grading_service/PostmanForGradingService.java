package com.CodeEvalCrew.AutoScore.services.postman_for_grading_service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanForGradingDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;
import com.CodeEvalCrew.AutoScore.repositories.postman_for_grading.PostmanForGradingRepository;

@Service
public class PostmanForGradingService implements IPostmanForGradingService {

    @Autowired
    private PostmanForGradingRepository postmanForGradingRepository;

    @Override
    public List<PostmanForGradingDTO> getPostmanForGradingByExamPaperId(Long examPaperId) {
        List<Postman_For_Grading> postmanForGradingEntries = postmanForGradingRepository.findByExamQuestion_ExamPaper_ExamPaperId(examPaperId);
        
        return postmanForGradingEntries.stream()
                .map(entry -> {
                    PostmanForGradingDTO dto = new PostmanForGradingDTO();
                    dto.setPostmanForGradingId(entry.getPostmanForGradingId());
                    dto.setPostmanFunctionName(entry.getPostmanFunctionName());
                    dto.setScoreOfFunction(entry.getScoreOfFunction());
                    dto.setTotalPmTest(entry.getTotalPmTest());
                    dto.setOrderBy(entry.getOrderBy());
                    dto.setPostmanForGradingParentId(entry.getPostmanForGradingParentId()); // Lấy từ thực thể
                    dto.setExamQuestionId(entry.getExamQuestion().getExamQuestionId());       // Lấy từ thực thể
                    dto.setGherkinScenarioId(entry.getGherkinScenario() != null ? entry.getGherkinScenario().getGherkinScenarioId() : null); // Lấy từ thực thể

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePostmanForGradingList(List<PostmanForGradingDTO> postmanForGradingDTOs) {
        for (PostmanForGradingDTO dto : postmanForGradingDTOs) {
            // Tìm kiếm thực thể theo ID
            Postman_For_Grading postmanForGrading = postmanForGradingRepository.findById(dto.getPostmanForGradingId())
                    .orElseThrow(() -> new RuntimeException("Postman_For_Grading not found with id: " + dto.getPostmanForGradingId()));
            
            // Cập nhật các trường từ DTO
            postmanForGrading.setScoreOfFunction(dto.getScoreOfFunction());
            postmanForGrading.setOrderBy(dto.getOrderBy());
            postmanForGrading.setPostmanForGradingParentId(dto.getPostmanForGradingParentId());

            // Lưu lại thực thể đã cập nhật
            postmanForGradingRepository.save(postmanForGrading);
        }
    }

}
