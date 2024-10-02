package com.CodeEvalCrew.AutoScore.repositories.examquestion_repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<Exam_Question, Long> {
    List<Exam_Question> findByExamPaper_ExamPaperId(long examPaperId);
}
