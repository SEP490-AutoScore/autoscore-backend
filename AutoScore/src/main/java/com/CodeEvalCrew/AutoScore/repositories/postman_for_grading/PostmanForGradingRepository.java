package com.CodeEvalCrew.AutoScore.repositories.postman_for_grading;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;

@Repository
public interface PostmanForGradingRepository extends JpaRepository<Postman_For_Grading, Long> {
    List<Postman_For_Grading> findByExamQuestion_ExamPaper_ExamPaperId(Long examPaperId);

    List<Postman_For_Grading> findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriorityAsc(Long examPaperId);

    List<Postman_For_Grading> findByExamPaper_ExamPaperId(Long examPaperId);

    Optional<Postman_For_Grading> findByPostmanFunctionName(String functionName);

    List<Postman_For_Grading> findByExamPaper_ExamPaperIdAndStatusTrue(Long examPaperId);

    List<Postman_For_Grading> findByExamQuestionAndStatusTrueOrderByOrderPriorityAsc(Exam_Question examQuestion);

    Optional<Postman_For_Grading> findByGherkinScenario_GherkinScenarioIdAndStatusTrue(Long gherkinScenarioId);

    List<Postman_For_Grading> findByPostmanFunctionNameInAndStatusTrue(List<String> functionNames);

    @Query("SELECT p.postmanFunctionName FROM Postman_For_Grading p WHERE p.status = true")
    List<String> findFunctionNamesByStatusTrue();

    @Query("SELECT COUNT(DISTINCT p.examQuestion.examQuestionId) " +
           "FROM Postman_For_Grading p WHERE p.postmanForGradingId IN :ids AND p.status = true")
    Long countDistinctExamQuestionIdsByIds(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Postman_For_Grading p WHERE p.examQuestion.examQuestionId = :examQuestionId AND p.status = true")
    List<Postman_For_Grading> findAllByExamQuestionIdAndStatusTrue(@Param("examQuestionId") Long examQuestionId);
}



