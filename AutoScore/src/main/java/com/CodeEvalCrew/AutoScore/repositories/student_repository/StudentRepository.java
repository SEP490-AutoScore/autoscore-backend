package com.CodeEvalCrew.AutoScore.repositories.student_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
}