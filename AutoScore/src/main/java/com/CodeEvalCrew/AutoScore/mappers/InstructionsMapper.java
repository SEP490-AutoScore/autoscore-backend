package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions.InstructionCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.InstructionView;
import com.CodeEvalCrew.AutoScore.models.Entity.Instructions;

@Mapper
public interface InstructionsMapper {
    InstructionsMapper INSTANCE = Mappers.getMapper(InstructionsMapper.class);

    InstructionView instructionToView(Instructions instruction);

    Instructions requestToEntity(InstructionCreateRequest request);
}
