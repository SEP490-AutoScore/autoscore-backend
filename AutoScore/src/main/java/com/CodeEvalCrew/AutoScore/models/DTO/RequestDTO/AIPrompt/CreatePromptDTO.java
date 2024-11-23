package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.AIPrompt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreatePromptDTO {
    private Long aiPromptId;

    private String content;

    private String languageCode;

    private String for_ai;

    private String type;
}
