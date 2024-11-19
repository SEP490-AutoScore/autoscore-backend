package com.CodeEvalCrew.AutoScore.services.important_service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ImportantMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Important.GetImportantRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ImportantView;
import com.CodeEvalCrew.AutoScore.models.Entity.Important;
import com.CodeEvalCrew.AutoScore.repositories.important_repository.ImportantRepository;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.ISubjectRepository;
import com.CodeEvalCrew.AutoScore.specification.ImportantSpecification;

@Service
public class ImportantService implements IImportantService{

    @Autowired
    private ImportantRepository importantRepository;

    @Autowired
    private ISubjectRepository subjectRepository;

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public List<ImportantView> getImportantOfSubject(GetImportantRequest request) throws Exception, NotFoundException {
        List<ImportantView> result;
        try {
            //check subject
            checkEntityExistence(subjectRepository.findById(request.getSubjectId()), "Subject", request.getSubjectId());

            //create spec to get
            Specification<Important> spec = ImportantSpecification.hasForeignKey(request.getSubjectId(), "subject", "subjectId");

            List<Important> importants = importantRepository.findAll(spec);

            //check result
            if (importants.isEmpty()) {
                throw new NoSuchElementException("No important with this subject");
            }

            //mapping result
            result = ImportantMapper.INSTANCE.fromListEntityToListView(importants);

            return result;
            
        } 
        catch (NoSuchElementException | NotFoundException nse) {
            throw nse;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    private <T> T checkEntityExistence(Optional<T> entity, String entityName, Long entityId) throws NotFoundException {
        return entity.orElseThrow(() -> new NotFoundException(entityName + " id: " + entityId + " not found"));
    }

}
