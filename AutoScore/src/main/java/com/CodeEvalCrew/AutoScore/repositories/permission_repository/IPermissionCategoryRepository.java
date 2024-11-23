package com.CodeEvalCrew.AutoScore.repositories.permission_repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Permission_Category;

@Repository
public interface IPermissionCategoryRepository extends JpaRepository<Permission_Category, Long>{
    Optional<Permission_Category> findByPermissionCategoryName(String name);
}
