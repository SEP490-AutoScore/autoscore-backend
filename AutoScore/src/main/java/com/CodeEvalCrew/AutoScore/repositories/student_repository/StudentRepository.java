package com.CodeEvalCrew.AutoScore.repositories.student_repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>,JpaSpecificationExecutor<Student> {
    Optional<Student> findByStudentCode(String studentCode);
    Optional<Student> findByStudentCodeAndExamExamId(String studentCode, Long examId);
    Optional<List<Student>> findAllByExamExamIdAndOrganizationName(Long examId, String organizationName);
    Optional<List<Student>> findAllByExamExamId(Long examId);
}