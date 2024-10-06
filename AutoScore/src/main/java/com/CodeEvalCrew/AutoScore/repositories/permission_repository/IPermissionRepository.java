package com.CodeEvalCrew.AutoScore.repositories.permission_repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Permission;

@Repository
public interface  IPermissionRepository extends JpaRepository<Permission, Long>{
    Optional<Permission> findByPermissionName(String name);
    Optional<Permission> findByAction(String action);
}
