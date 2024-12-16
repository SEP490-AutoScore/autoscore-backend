package com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;

@Repository
public interface IExamDatabaseRepository extends JpaRepository<Exam_Database, Long> {
    Exam_Database findByExamPaperExamPaperId(Long examPaperId);

    // Fetch only the databaseName based on examPaperId
    @Query("SELECT e.databaseName FROM Exam_Database e WHERE e.examPaper.examPaperId = :examPaperId")
    String findDatabaseNameByExamPaperId(@Param("examPaperId") Long examPaperId);

    // Tìm Exam_Database dựa vào examQuestionId
    @Query("SELECT e FROM Exam_Database e JOIN e.examPaper p JOIN p.examQuestions q WHERE q.examQuestionId = :examQuestionId")
    Optional<Exam_Database> findByExamQuestionId(@Param("examQuestionId") Long examQuestionId);

    Optional<Exam_Database> findByExamPaper_ExamPaperId(Long examPaperId);


    @Query("SELECT e FROM Exam_Database e WHERE e.examPaper.examPaperId = :examPaperId")
    Optional<Exam_Database> findByExamPaperId(@Param("examPaperId") Long examPaperId);

   


}
