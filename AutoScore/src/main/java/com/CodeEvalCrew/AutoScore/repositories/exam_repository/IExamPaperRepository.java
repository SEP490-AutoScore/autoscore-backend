package com.CodeEvalCrew.AutoScore.repositories.exam_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;

@Repository
public interface IExamPaperRepository extends JpaRepository<Exam_Paper, Long>, JpaSpecificationExecutor<Exam_Paper> {
    
}
