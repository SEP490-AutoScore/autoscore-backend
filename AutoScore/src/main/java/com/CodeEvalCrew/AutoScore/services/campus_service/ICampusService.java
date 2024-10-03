package com.CodeEvalCrew.AutoScore.services.campus_service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CampusRequest.CreateCampusRequest;
import com.CodeEvalCrew.AutoScore.models.Entity.Campus;

public interface ICampusService {
    Campus createCampus(CreateCampusRequest request);
}
