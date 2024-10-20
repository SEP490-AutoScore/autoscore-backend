package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstructionCreateRequest {
    private String instructionName;
    private String introduction;
    private String important;
    private Long subjectId;
}
