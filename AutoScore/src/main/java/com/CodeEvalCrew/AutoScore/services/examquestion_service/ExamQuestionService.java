package com.CodeEvalCrew.AutoScore.services.examquestion_service;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.repositories.examquestion_repository.ExamQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamQuestionService implements IExamQuestionService {

    @Autowired
    private ExamQuestionRepository examQuestionRepository;

    @Override
    public List<Exam_Question> getExamQuestionsByExamPaperId(long examPaperId) {
        return examQuestionRepository.findByExamPaper_ExamPaperId(examPaperId);
    }
}
