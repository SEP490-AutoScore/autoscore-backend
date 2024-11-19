package com.CodeEvalCrew.AutoScore.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ImportantView;
import com.CodeEvalCrew.AutoScore.models.Entity.Important;

@Mapper
public interface ImportantMapper {
    ImportantMapper INSTANCE = Mappers.getMapper(ImportantMapper.class);

    List<ImportantView> fromListEntityToListView(List<Important> importants);
}
