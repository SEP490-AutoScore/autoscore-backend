package com.CodeEvalCrew.AutoScore.repositories.subject_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISubjectRepository extends JpaRepository<Subject, Long> {
    Page<Subject> findAll(Pageable pageable);

    Page<Subject> findBySubjectCode(String subjectCode, Pageable pageable);

}
