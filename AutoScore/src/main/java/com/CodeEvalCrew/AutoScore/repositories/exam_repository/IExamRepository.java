package com.CodeEvalCrew.AutoScore.repositories.exam_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam;

@Repository
public interface  IExamRepository extends  JpaRepository<Exam, Long>,JpaSpecificationExecutor<Exam> {
    
}
