package com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IExamDatabaseRepository extends JpaRepository<Exam_Database, Long> {
}
