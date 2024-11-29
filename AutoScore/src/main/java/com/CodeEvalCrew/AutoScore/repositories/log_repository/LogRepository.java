package com.CodeEvalCrew.AutoScore.repositories.log_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Log;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    
}
