package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionResponseDTO {
    private Long positionId;
    private String name;
    private boolean status;
}
