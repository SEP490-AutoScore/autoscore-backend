package com.CodeEvalCrew.AutoScore.repositories.exam_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Barem;

@Repository
public interface IExamBaremRepository extends JpaRepository<Exam_Barem, Long>,JpaSpecificationExecutor<Exam_Barem> {
    List<Exam_Barem> getByExamQuestionExamQuestionId(Long examQuestionId);
}
