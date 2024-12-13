package com.CodeEvalCrew.AutoScore.repositories.semester_repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.CodeEvalCrew.AutoScore.models.Entity.Semester;

public interface SemesterRepository extends JpaRepository<Semester, Long>{
    boolean existsBySemesterCode(String semesterCode);
}
