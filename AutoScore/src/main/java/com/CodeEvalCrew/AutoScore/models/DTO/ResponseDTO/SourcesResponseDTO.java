package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourcesResponseDTO {
    private String examCode;
    private String examPaperCode;
    private String subjectName;
    private String subjectCode;
    private String sourcePath;
    private List<SourceDetailsResponseDTO> sourceDetails;
    private List<StudentErrorResponseDTO> studentErrors;
}
