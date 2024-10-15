package com.CodeEvalCrew.AutoScore.mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamDatabaseResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExamDatabaseMapper {
    ExamDatabaseMapper INSTANCE = Mappers.getMapper(ExamDatabaseMapper.class);

    @Mapping(source = "examDatabaseId", target = "examDatabaseId")
    @Mapping(source = "databaseScript", target = "databaseScript")
    @Mapping(source = "databaseDescription", target = "databaseDescription")
    @Mapping(source = "databaseName", target = "databaseName")
    @Mapping(source = "databaseImage", target = "databaseImage")
    @Mapping(source = "databaseNote", target = "databaseNote")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "updatedBy", target = "updatedBy")
    @Mapping(source = "deletedAt", target = "deletedAt")
    @Mapping(source = "deletedBy", target = "deletedBy")
    ExamDatabaseResponseDTO toDTO(Exam_Database examDatabase);
}
