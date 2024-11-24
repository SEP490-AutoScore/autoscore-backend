package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;


import java.time.LocalDateTime;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.AIName_Enum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIApiKeyDTO {
    private Long aiApiKeyId;
     private AIName_Enum aiName;
    private String aiApiKey; 
    private boolean status;
    private boolean isShared;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String fullName; 
    private boolean isSelected;
}