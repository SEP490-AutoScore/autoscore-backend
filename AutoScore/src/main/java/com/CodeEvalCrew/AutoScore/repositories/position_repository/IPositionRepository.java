package com.CodeEvalCrew.AutoScore.repositories.position_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Position;

@Repository
public interface IPositionRepository extends JpaRepository<Position, Long> {
    boolean existsByEmployeesPosition(Position position);
}
