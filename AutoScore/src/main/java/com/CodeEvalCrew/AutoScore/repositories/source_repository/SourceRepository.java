package com.CodeEvalCrew.AutoScore.repositories.source_repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
}