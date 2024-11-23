package com.CodeEvalCrew.AutoScore.repositories.role_repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Role;

@Repository
public interface IRoleRepositoty extends JpaRepository<Role, Long>{
    Optional<Role> findByRoleName(String name);
}
