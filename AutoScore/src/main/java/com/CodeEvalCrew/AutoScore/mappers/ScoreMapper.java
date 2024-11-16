package com.CodeEvalCrew.AutoScore.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperDTOforAutoscore;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentDTOforAutoscore;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Score;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;

@Mapper(componentModel = "spring")
public interface ScoreMapper {

    ScoreMapper INSTANCE = Mappers.getMapper(ScoreMapper.class);

    Student toStudent(StudentDTOforAutoscore dto);

    Exam_Paper toExamPaper(ExamPaperDTOforAutoscore dto);

    @Mapping(source = "student.studentCode", target = "studentCode")
    @Mapping(source = "student.studentEmail", target = "studentEmail")
    @Mapping(source = "examPaper.examPaperCode", target = "examPaperCode")
    @Mapping(source = "totalScore", target = "totalScore")
    @Mapping(source = "reason", target = "reason")
    @Mapping(source = "gradedAt", target = "gradedAt")
    @Mapping(source = "levelOfPlagiarism", target = "levelOfPlagiarism")
    @Mapping(source = "plagiarismReason", target = "plagiarismReason")
    // @Mapping(source = "codePlagiarisms", target = "codePlagiarisms")
    ScoreResponseDTO toScoreResponseDTO(Score score);

    List<ScoreResponseDTO> scoreEntityToScoreResponseDTO(List<Score> scores);
}
