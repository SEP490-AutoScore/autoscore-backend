package com.CodeEvalCrew.AutoScore.repositories.important_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Important;

@Repository
public interface  ImportantRepository extends JpaRepository<Important, Long>, JpaSpecificationExecutor<Important> {
}
