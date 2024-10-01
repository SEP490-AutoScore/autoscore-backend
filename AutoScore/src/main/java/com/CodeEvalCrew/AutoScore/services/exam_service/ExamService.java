package com.CodeEvalCrew.AutoScore.services.exam_service;

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
    public ReponseEntity<Exam> getExamById(long id) throws Exception{
        ReponseEntity<Exam> result = new ReponseEntity<>();
        try {
            Exam exam = examRepository.findById(id).get();
            if (exam == null) {
                result.setMessage("Not found exam with id: "+ id);
                throw new Exception("Not found");
            }
            result.ok(exam);
        } catch (Exception e) {
            throw new Exception("Not found");
            // return result;
        }

        return result;
    }
}