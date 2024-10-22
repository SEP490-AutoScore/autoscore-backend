package com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;

@Repository
public interface IExamDatabaseRepository extends JpaRepository<Exam_Database, Long> {
    Exam_Database findByExamPaperExamPaperId(Long examPaperId);
}
