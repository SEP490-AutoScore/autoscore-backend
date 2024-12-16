package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceDetailsResponseDTO {
    private String studentCode;
    private String studentEmail;
    private String sourcePath;
    private String type;
}
