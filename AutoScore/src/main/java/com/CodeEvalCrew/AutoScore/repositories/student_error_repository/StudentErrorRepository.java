package com.CodeEvalCrew.AutoScore.repositories.student_error_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Student_Error;

@Repository
public interface StudentErrorRepository extends JpaRepository<Student_Error, Long> {
    List<Student_Error> findBySourceSourceId(Long sourceId);
    void deleteAllBySourceSourceId(Long sourceId);
}
