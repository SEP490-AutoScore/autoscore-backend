package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import lombok.Data;

@Data
public class PositionRequestDTO {
    private Long positionId;
    private String name;
    private String description;
    private boolean status;
}
