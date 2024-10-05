package com.CodeEvalCrew.AutoScore.services.subject_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.CreateSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Department;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.ISubjectRepository;
import com.CodeEvalCrew.AutoScore.repositories.department_repository.IDepartmentRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class SubjectService implements ISubjectService {

    @Autowired
    private ISubjectRepository subjectRepository;

    @Autowired
    private IDepartmentRepository departmentRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Override
    public Subject createSubject(CreateSubjectRequest request) {
        Subject subject = new Subject();
        subject.setSubjectName(request.getSubjectName());
        subject.setSubjectCode(request.getSubjectCode());
        subject.setStatus(true); // Automatically set status to true
        subject.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        // Fetch the department and account based on their IDs
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
       

        // Set the department and account objects in the subject
        subject.setDepartment(department);
        

        return subjectRepository.save(subject);
    }

    @Override
    public Page<Subject> getSubjectByCode(String subjectCode, Pageable pageable) {
        // Here we assume that we can retrieve a paginated list by subject code.
        // This would typically mean filtering, which may return more than one subject.
        return subjectRepository.findBySubjectCode(subjectCode, pageable);
    }

    @Override
    public Page<Subject> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable);
    }

    @Override
    public Subject updateSubject(long id, CreateSubjectRequest request) {
        Optional<Subject> optionalSubject = subjectRepository.findById(id);
        if (optionalSubject.isPresent()) {
            Subject subject = optionalSubject.get();
            subject.setSubjectName(request.getSubjectName());
            subject.setSubjectCode(request.getSubjectCode());

            subject.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            return subjectRepository.save(subject);
        } else {
            throw new RuntimeException("Subject not found");
        }
    }

    @Override
    public void deleteSubject(long id) {
        Optional<Subject> subject = subjectRepository.findById(id);
        subject.ifPresent(subjectRepository::delete);
    }
}
