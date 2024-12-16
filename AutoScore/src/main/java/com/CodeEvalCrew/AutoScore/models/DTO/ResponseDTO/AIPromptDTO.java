package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;


import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Purpose_Enum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIPromptDTO {
     private Long aiPromptId;
    private String questionAskAiContent;
    private Long orderPriority;
    private Purpose_Enum purpose;
}
