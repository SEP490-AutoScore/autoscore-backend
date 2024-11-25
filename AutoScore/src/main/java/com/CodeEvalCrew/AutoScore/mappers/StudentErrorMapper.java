package com.CodeEvalCrew.AutoScore.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SourceDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentDTOforAutoscore;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentErrorResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.models.Entity.Student_Error;

@Mapper(componentModel = "spring")
public interface StudentErrorMapper {

    StudentErrorMapper INSTANCE = Mappers.getMapper(StudentErrorMapper.class);

    Student toStudent(StudentDTOforAutoscore dto);

    Source toSource(SourceDTO dto);

    StudentErrorResponseDTO toStudentErrorResponseDTO(Student_Error studentError);

    List<StudentErrorResponseDTO> studentErrorEntityToStudentErrorResponseDTO(List<Student_Error> studentErrors);
}
