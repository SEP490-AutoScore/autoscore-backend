package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamPaperFilePostmanResponseDTO {
    private String fileCollectionPostman;
    private Boolean isComfirmFile;
    private Long totalItem;
    private String logRunPostman; 
}
