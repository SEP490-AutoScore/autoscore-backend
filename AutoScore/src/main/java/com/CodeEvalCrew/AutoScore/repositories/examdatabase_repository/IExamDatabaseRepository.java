package com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository;

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
}
