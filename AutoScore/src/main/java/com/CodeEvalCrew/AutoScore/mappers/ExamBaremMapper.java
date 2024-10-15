package com.CodeEvalCrew.AutoScore.mappers;

import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem.ExamBaremCreate;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem.ExamBaremViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamBaremView;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Barem;

public interface ExamBaremMapper {
    ExamBaremMapper INSTANCE = Mappers.getMapper(ExamBaremMapper.class);

    ExamBaremView examBaremToView(Exam_Barem examBarem);
    Exam_Barem examBaremViewRequestToEntity(ExamBaremViewRequest request);
    Exam_Barem examBaremCreateRequestToEntity(ExamBaremCreate request);
}
