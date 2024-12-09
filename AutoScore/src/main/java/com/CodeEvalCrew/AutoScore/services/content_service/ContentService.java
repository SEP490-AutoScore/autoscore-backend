package com.CodeEvalCrew.AutoScore.services.content_service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ContentDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Content;
import com.CodeEvalCrew.AutoScore.repositories.content_repository.ContentRepository;

@Service
public class ContentService implements IContentService  {

    @Autowired
    private ContentRepository contentRepository;

    @Override
    public List<ContentDTO> getAllContent() {
     
        List<Content> contentList = contentRepository.findAllByOrderByPurposeAscOrderPriorityAsc();

        return contentList.stream()
                .map(content -> new ContentDTO(
                        content.getContentId(),
                        content.getQuestionAskAiContent(),
                        content.getOrderPriority(),
                        content.getPurpose()
                ))
                .collect(Collectors.toList());
    }

    @Override
      public void updateQuestionAskAiContent(Long contentId, String newQuestionAskAiContent) {

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found with ID: " + contentId));

        content.setQuestionAskAiContent(newQuestionAskAiContent);

        contentRepository.save(content);
    }
}