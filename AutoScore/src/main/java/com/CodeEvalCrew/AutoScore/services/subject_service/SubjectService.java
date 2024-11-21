package com.CodeEvalCrew.AutoScore.services.subject_service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SubjectView;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.ISubjectRepository;

@Service
public class SubjectService implements ISubjectService{
    @Autowired
    private ISubjectRepository subjectRepository;

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
}
