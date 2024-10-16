package com.CodeEvalCrew.AutoScore.repositories.student_error_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Student_Error;

@Repository
public interface StudentErrorRepository extends JpaRepository<Student_Error, Long>{
    
}
