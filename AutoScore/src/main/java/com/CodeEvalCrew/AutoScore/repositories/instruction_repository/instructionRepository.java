package com.CodeEvalCrew.AutoScore.repositories.instruction_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.CodeEvalCrew.AutoScore.models.Entity.Instructions;

public interface instructionRepository extends JpaRepository<Instructions, Long>, JpaSpecificationExecutor<Instructions> {

}
