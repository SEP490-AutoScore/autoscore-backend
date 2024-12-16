package com.CodeEvalCrew.AutoScore.repositories.source_repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Source;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    Optional<Source> findByExamPaper_ExamPaperId(Long examPaperId);
    Optional<List<Source>> findAllByExamPaperExamPaperId(Long examPaperId);
    Optional<Source> findByOriginSourcePath(String originSourcePath);
}