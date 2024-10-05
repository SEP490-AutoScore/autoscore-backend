package com.CodeEvalCrew.AutoScore.repositories.department_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.CodeEvalCrew.AutoScore.models.Entity.Department;

public interface IDepartmentRepository extends JpaRepository<Department, Long> {
}
