package com.CodeEvalCrew.AutoScore.services.instruction_service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions.InstructionCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions.InstructionViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.InstructionView;

@Service
public class IntructionService implements IIntructionService{

    @Override
    public InstructionView getById(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<InstructionView> getList(InstructionViewRequest request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InstructionView createNewInstruoction(InstructionCreateRequest request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InstructionView updateInstruction(Long id, InstructionCreateRequest request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
