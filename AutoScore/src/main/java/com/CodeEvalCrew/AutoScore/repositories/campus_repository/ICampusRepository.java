package com.CodeEvalCrew.AutoScore.repositories.campus_repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.CodeEvalCrew.AutoScore.models.Entity.Campus;

public interface ICampusRepository extends JpaRepository<Campus, Long> {
}
