package com.CodeEvalCrew.AutoScore.repositories.department_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.CodeEvalCrew.AutoScore.models.Entity.Department;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IDepartmentRepository extends JpaRepository<Department, Long> {
    Page<Department> findByDevLanguageIgnoreCase(String devLanguage, Pageable pageable);
@Query("SELECT d FROM Department d WHERE d.devLanguage = :devLanguage")
List<Department> findByDevLanguage(@Param("devLanguage") String devLanguage);

}
