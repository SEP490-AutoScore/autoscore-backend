package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;


import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIApiKeyDTO {
    private Long aiApiKeyId;
    private String aiName;
    private String aiApiKey; 
    private boolean status;
    private boolean isShared;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long accountId; 
    private boolean isSelected;
}