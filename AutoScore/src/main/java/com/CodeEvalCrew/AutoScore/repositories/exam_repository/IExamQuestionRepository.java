package com.CodeEvalCrew.AutoScore.repositories.exam_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;

@Repository
public interface IExamQuestionRepository extends JpaRepository<Exam_Question,Long>, JpaSpecificationExecutor<Exam_Question> {
    List<Exam_Question> getByExamPaperExamPaperId(Long examPaperId);
    // Optional<Exam_Question> findByPostmanForGradingId(Long postmanForGradingId);
    List<Exam_Question> findByExamPaper_ExamPaperId(Long examPaperId);

    // Optional<Exam_Question> findByExamPaper_ExamPaperIdAndHttpMethodAndEndPoint(Long examPaperId, String httpMethod, String endPoint);

       @Query("SELECT eq FROM Exam_Question eq WHERE eq.examPaper.examPaperId = :examPaperId")
    List<Exam_Question> findByExamPaperId(@Param("examPaperId") Long examPaperId);
}