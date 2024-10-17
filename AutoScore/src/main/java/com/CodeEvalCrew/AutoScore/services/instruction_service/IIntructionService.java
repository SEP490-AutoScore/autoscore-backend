package com.CodeEvalCrew.AutoScore.services.instruction_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions.InstructionCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions.InstructionViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.InstructionView;

public interface IIntructionService {

    public InstructionView getById(Long id);

    public List<InstructionView> getList(InstructionViewRequest request);

    public InstructionView createNewInstruoction(InstructionCreateRequest request);

    public InstructionView updateInstruction(Long id, InstructionCreateRequest request);

}
