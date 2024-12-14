package com.CodeEvalCrew.AutoScore.services.position_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PositionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PositionResponseDTO;

public interface IPositionService {
    List<PositionResponseDTO> getAllPosition();
    PositionResponseDTO getPositionById(Long positionId);
    OperationStatus createPosition(PositionRequestDTO positionRequestDTO);
    OperationStatus updatePosition(PositionRequestDTO positionRequestDTO);
    OperationStatus deletePosition(Long positionId);
    List<PositionResponseDTO> getAllPositionByRole();
}
