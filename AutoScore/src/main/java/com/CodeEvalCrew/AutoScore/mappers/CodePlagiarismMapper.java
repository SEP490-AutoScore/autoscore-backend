package com.CodeEvalCrew.AutoScore.mappers;

import com.CodeEvalCrew.AutoScore.models.Entity.Code_Plagiarism;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.CodePlagiarismResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CodePlagiarismMapper {

    CodePlagiarismMapper INSTANCE = Mappers.getMapper(CodePlagiarismMapper.class);

    // @Mapping(source = "selfCode", target = "selfCode")
    CodePlagiarismResponseDTO toCodePlagiarismResponseDTO(Code_Plagiarism entity);

    List<CodePlagiarismResponseDTO> toCodePlagiarismResponseDTOList(List<Code_Plagiarism> entities);
}
