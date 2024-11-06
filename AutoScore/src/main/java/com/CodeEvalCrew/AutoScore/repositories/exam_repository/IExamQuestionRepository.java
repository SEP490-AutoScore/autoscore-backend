package com.CodeEvalCrew.AutoScore.repositories.exam_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;

@Repository
public interface IExamQuestionRepository extends JpaRepository<Exam_Question,Long>, JpaSpecificationExecutor<Exam_Question> {
    List<Exam_Question> getByExamPaperExamPaperId(Long examPaperId);
    // Optional<Exam_Question> findByPostmanForGradingId(Long postmanForGradingId);
}