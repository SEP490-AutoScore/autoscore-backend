package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;

@Mapper
public interface ExamMapper {
    ExamMapper INSTANCE = Mappers.getMapper(ExamMapper.class);

    @Mapping(source = "examId", target = "examId")
    @Mapping(source = "examCode", target = "examCode")
    @Mapping(source = "examAt", target = "examAt")
    @Mapping(source = "gradingAt", target = "gradingAt")
    @Mapping(source = "publishAt", target = "publishAt")
    @Mapping(source = "semesterName", target = "semesterName")

    ExamViewResponseDTO examToViewResponse(Exam exam);

    Exam requestToExam(ExamCreateRequestDTO examCreateRequestDTO);
}