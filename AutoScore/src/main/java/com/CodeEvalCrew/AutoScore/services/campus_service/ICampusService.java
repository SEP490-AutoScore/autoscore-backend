package com.CodeEvalCrew.AutoScore.services.campus_service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CampusRequest.CreateCampusRequest;
import com.CodeEvalCrew.AutoScore.models.Entity.Campus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICampusService {
    Campus createCampus(CreateCampusRequest request);

    Page<Campus> getAllCampuses(Pageable pageable);
}
