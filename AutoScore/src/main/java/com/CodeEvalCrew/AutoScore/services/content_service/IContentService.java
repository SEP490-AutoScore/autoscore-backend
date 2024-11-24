package com.CodeEvalCrew.AutoScore.services.content_service;


import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ContentDTO;
public interface IContentService {
    List<ContentDTO> getAllContent();
}
