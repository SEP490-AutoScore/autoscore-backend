package com.CodeEvalCrew.AutoScore.services.subject_service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.CreateSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.DeleteSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.UpdateSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISubjectService {
    Subject createSubject(CreateSubjectRequest request);

    Page<Subject> getSubjectByCode(String subjectCode, Pageable pageable);

    Page<Subject> getAllSubjects(Pageable pageable);

    Subject updateSubject(UpdateSubjectRequest request);

     void deleteSubject(DeleteSubjectRequest request);
}
