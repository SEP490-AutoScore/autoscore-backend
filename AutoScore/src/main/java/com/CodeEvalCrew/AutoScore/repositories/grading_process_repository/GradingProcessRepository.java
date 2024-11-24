package com.CodeEvalCrew.AutoScore.repositories.grading_process_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.CodeEvalCrew.AutoScore.models.Entity.GradingProcess;

public interface GradingProcessRepository extends JpaRepository<GradingProcess,Long>, JpaSpecificationExecutor<GradingProcess>{
    
}
