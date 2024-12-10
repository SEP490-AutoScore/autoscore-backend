package com.CodeEvalCrew.AutoScore.services.subject_service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.SubjectMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.CreateSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SubjectView;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization_Subject;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import com.CodeEvalCrew.AutoScore.repositories.organization_repository.IOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.ISubjectRepository;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.SubjectOrgenizationRepository;

@Service
public class SubjectService implements ISubjectService{
    @Autowired
    private ISubjectRepository subjectRepository;
    @Autowired
    private SubjectOrgenizationRepository subjectOrganizationRepository;
    @Autowired
    private IOrganizationRepository organizationRepository;

    @Override
    public List<SubjectView> getAllSubject() {
        List<SubjectView> result = new ArrayList<>();
        try {
            List<Subject> subjects = subjectRepository.findAll();
            
            if (subjects.isEmpty()) {
                throw new NoSuchElementException("No subject has found");
            }

            for (Subject subject : subjects) {
                result.add(new SubjectView(subject.getSubjectId(), subject.getSubjectCode(), subject.getSubjectName()));
            }

            return result;
        } catch (NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public SubjectView createNewSubject(CreateSubjectRequest request) {
        SubjectView result;
        try {
                Subject subject = new Subject();
                subject.setSubjectName(request.getSubjectName());
                subject.setSubjectCode(request.getSubjectCode());

                subjectRepository.save(subject);
                
                result = SubjectMapper.INSTANCE.subjectToView(subject);

            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public SubjectView addSubjectintoOrganization(Long organizationId, Long subjectId) throws Exception, NotFoundException{
        SubjectView result;
        try {
                Organization_Subject entity = new Organization_Subject();
                Subject subject = checkEntityExistence(subjectRepository.findById(subjectId),"Subject", subjectId);
                Organization org = checkEntityExistence(organizationRepository.findById(organizationId), "Organization", organizationId);
                if(org.getType().equals(Organization_Enum.CAMPUS)) throw new NotFoundException("CAMPUS not found");
                entity.setOrganization(org);
                entity.setSubject(subject);
                entity.setStatus(true);
                subjectOrganizationRepository.save(entity);

                result = SubjectMapper.INSTANCE.subjectToView(subject);
                return result;
        } catch (Exception | NotFoundException e) {
            throw e;
        }
    }

    //check entiy func helper
    private <T> T checkEntityExistence(Optional<T> entity, String entityName, Long entityId) throws NotFoundException {
        return entity.orElseThrow(() -> new NotFoundException(entityName + " id: " + entityId + " not found"));
    }
}
