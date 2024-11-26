package com.CodeEvalCrew.AutoScore.services.content_service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ContentDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Content;
import com.CodeEvalCrew.AutoScore.repositories.content_repository.ContentRepository;

@Service
public class ContentService {

    @Autowired
    private ContentRepository contentRepository;

    public List<ContentDTO> getAllContent() {
        // Lấy tất cả Content sắp xếp theo purpose và orderPriority
        List<Content> contentList = contentRepository.findAllByOrderByPurposeAscOrderPriorityAsc();

        // Chuyển đổi từ Content sang ContentDTO
        return contentList.stream()
                .map(content -> new ContentDTO(
                        content.getContentId(),
                        content.getQuestionAskAiContent(),
                        content.getOrderPriority(),
                        content.getPurpose()
                ))
                .collect(Collectors.toList());
    }
}