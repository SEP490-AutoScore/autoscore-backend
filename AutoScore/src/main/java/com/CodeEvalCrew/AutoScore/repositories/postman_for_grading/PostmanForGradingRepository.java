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

    // Truy vấn danh sách Postman_For_Grading theo examPaperId và sắp xếp theo
    // orderBy
    @Query("SELECT p FROM Postman_For_Grading p WHERE p.examPaper.examPaperId = :examPaperId ORDER BY p.orderBy")
    List<Postman_For_Grading> findByExamPaperIdOrderByOrderBy(@Param("examPaperId") Long examPaperId);

    List<Postman_For_Grading> findByExamPaper_ExamPaperId(Long examPaperId);

    Optional<Postman_For_Grading> findByPostmanFunctionName(String functionName);

    List<Postman_For_Grading> findByExamPaper_ExamPaperIdAndStatusTrue(Long examPaperId);

    List<Postman_For_Grading> findByExamQuestionAndStatusTrue(Exam_Question examQuestion);

    // Optional<Postman_For_Grading> findByGherkinScenario_GherkinScenarioId(Long gherkinScenarioId);

    
    Optional<Postman_For_Grading> findByGherkinScenario_GherkinScenarioIdAndStatusTrue(Long gherkinScenarioId);

    
    

}
