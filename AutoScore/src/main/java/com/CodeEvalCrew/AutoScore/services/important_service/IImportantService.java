package com.CodeEvalCrew.AutoScore.services.important_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Important.GetImportantRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ImportantView;

public interface IImportantService {

    List<ImportantView> getImportantOfSubject(GetImportantRequest request) throws Exception, NotFoundException;

}
