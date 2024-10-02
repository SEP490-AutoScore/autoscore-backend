package com.CodeEvalCrew.AutoScore.services.exam_service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ReponseEntity;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;

@Service
public class ExamService implements IExamService{
    private final IExamRepository examRepository;

    public ExamService(IExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Override
    public Exam getExamById(long id) throws Exception{
        Exam result = new Exam();
        try {
            Exam exam = examRepository.findById(id).get();
            if (exam == null) {
                result.setMessage("Not found exam with id: "+ id);
                throw new Exception("Not found");
            }
            result = exam;
        } catch (Exception e) {
            throw new Exception("Not found");
            // return result;
        }

        return result;
    }
}