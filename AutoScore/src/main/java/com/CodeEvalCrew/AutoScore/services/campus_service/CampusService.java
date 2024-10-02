package com.CodeEvalCrew.AutoScore.services.campus_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CampusRequest.CreateCampusRequest;
import com.CodeEvalCrew.AutoScore.models.Entity.Campus;
import com.CodeEvalCrew.AutoScore.repositories.campus_repository.ICampusRepository;

@Service
public class CampusService implements ICampusService {

    @Autowired
    private ICampusRepository campusRepository;

    @Override
    public Campus createCampus(CreateCampusRequest request) {
        Campus campus = new Campus();
        campus.setCampusName(request.getCampusName());
        campus.setStatus(request.isStatus());
        return campusRepository.save(campus);
    }
}
