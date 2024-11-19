package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImportantView {
    private Long importantId;
    private String importantName;
    private String importantCode;
    @Lob
    private String importantScrip;
    private SubjectView subject;
}
