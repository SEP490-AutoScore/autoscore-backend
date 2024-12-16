package com.CodeEvalCrew.AutoScore.services.subject_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.CreateSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SubjectView;

public interface ISubjectService {

    List<SubjectView> getAllSubject();

    SubjectView createNewSubject(CreateSubjectRequest request);

    SubjectView addSubjectintoOrganization(Long organizationId, Long subjectId) throws Exception, NotFoundException;

    SubjectView updateInfoSubject(Long subjectId, CreateSubjectRequest request) throws Exception, NotFoundException;

    SubjectView getSubjectBySubjectId(Long subjectId) throws Exception;

}
