package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CampusResponseDTO {
    private Long campusId;
    private String campusName;
    private boolean status;
}
