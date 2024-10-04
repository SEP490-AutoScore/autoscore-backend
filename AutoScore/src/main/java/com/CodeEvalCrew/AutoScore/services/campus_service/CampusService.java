package com.CodeEvalCrew.AutoScore.services.campus_service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

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
        campus.setStatus(true); // Automatically set status to true
        return campusRepository.save(campus);
    }

    @Override
    public Page<Campus> getAllCampuses(Pageable pageable) {
        return campusRepository.findAll(pageable); // Return paginated results
    }

    @Override
    public Campus updateCampusName(long id, CreateCampusRequest request) {
        Optional<Campus> optionalCampus = campusRepository.findById(id);
        if (optionalCampus.isPresent()) {
            Campus campus = optionalCampus.get();
            campus.setCampusName(request.getCampusName()); // Update the name
            return campusRepository.save(campus); // Save updated entity
        } else {
            throw new RuntimeException("Campus not found with ID: " + id);
        }
    }
}
