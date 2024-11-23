package com.CodeEvalCrew.AutoScore.repositories.permission_repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Permission;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission_Category;

@Repository
public interface  IPermissionRepository extends JpaRepository<Permission, Long>{
    Optional<Permission> findByPermissionName(String name);
    Optional<Permission> findByAction(String action);
    Optional<List<Permission>> findAllByPermissionCategory(Permission_Category permissionCategory);
}
