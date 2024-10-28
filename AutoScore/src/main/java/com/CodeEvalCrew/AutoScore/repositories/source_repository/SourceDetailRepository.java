package com.CodeEvalCrew.AutoScore.repositories.source_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;

@Repository
public interface SourceDetailRepository extends JpaRepository<Source_Detail, Long> {
    List<Source_Detail> findBySource_ExamPaper_ExamPaperIdOrderByStudent_StudentId(Long examPaperId);
}