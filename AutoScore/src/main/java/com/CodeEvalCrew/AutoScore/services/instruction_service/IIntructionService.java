package com.CodeEvalCrew.AutoScore.services.instruction_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions.InstructionCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions.InstructionViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.InstructionView;

public interface IIntructionService {

    public InstructionView getById(Long id) throws NotFoundException;

    public List<InstructionView> getList(InstructionViewRequest request)  throws NotFoundException;

    public InstructionView createNewInstruoction(InstructionCreateRequest request) throws NotFoundException;

    public InstructionView updateInstruction(Long id, InstructionCreateRequest request) throws NotFoundException;

}
