package com.CodeEvalCrew.AutoScore.repositories.source_repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceDetailRepository extends JpaRepository<Source_Detail, Long> {
}