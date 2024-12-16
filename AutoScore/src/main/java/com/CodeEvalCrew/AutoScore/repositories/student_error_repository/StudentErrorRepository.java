package com.CodeEvalCrew.AutoScore.repositories.student_error_repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.models.Entity.Student_Error;

@Repository
public interface StudentErrorRepository extends JpaRepository<Student_Error, Long> {
    List<Student_Error> findBySourceSourceId(Long sourceId);
    Optional<Student_Error> findBySourceAndStudent(Source source, Student student);
    void deleteAllBySourceSourceId(Long sourceId);
}
