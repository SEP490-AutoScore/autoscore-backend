package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionResponseDTO {
    private Long positionId;
    private String name;
    private String description;
    private boolean status;
    private LocalDateTime lastUpdated;
    private int totalUser;
}
