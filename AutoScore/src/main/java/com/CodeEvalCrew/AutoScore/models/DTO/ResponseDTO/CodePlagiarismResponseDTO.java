package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.Data;

@Data
public class CodePlagiarismResponseDTO {

    private Long codePlagiarismId;
    private String selfCode;
    private String studentCodePlagiarism;
    private String plagiarismPercentage;
    private String studentPlagiarism;
    private String type;
}
