package com.CodeEvalCrew.AutoScore.mappers;

import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentDTO;

import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentDTO toDTO(Student student);

    Student toEntity(StudentDTO dto);
}