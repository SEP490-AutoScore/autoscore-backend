package com.CodeEvalCrew.AutoScore.services.position_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PositionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PositionResponseDTO;

public interface IPositionService {
    List<PositionResponseDTO> getAllPosition();
    public PositionResponseDTO getPositionById(Long positionId);
    public OperationStatus createPosition(PositionRequestDTO positionRequestDTO);
    public OperationStatus updatePosition(PositionRequestDTO positionRequestDTO);
    public OperationStatus deletePosition(Long positionId);
}
