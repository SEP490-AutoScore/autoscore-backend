package com.CodeEvalCrew.AutoScore.services.exam_barem_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem.ExamBaremCreate;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem.ExamBaremViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamBaremView;

public interface IExamBaremService {

    ExamBaremView getExamById(Long id) throws NotFoundException;

    List<ExamBaremView> getList(ExamBaremViewRequest request);

    ExamBaremView createNewExamBarem(ExamBaremCreate request) throws NotFoundException;

    ExamBaremView updateExamBarem(Long id, ExamBaremCreate request) throws NotFoundException;
    
}
