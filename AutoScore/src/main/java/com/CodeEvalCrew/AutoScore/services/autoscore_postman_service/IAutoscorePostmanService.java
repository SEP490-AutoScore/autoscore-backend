package com.CodeEvalCrew.AutoScore.services.autoscore_postman_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.StudentSourceInfoDTO;

public interface IAutoscorePostmanService {
    List<StudentSourceInfoDTO> gradingFunction(Long examPaperId, int numberDeploy);

}
