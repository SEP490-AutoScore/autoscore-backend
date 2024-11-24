package com.CodeEvalCrew.AutoScore.repositories.code_plagiarism_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Code_Plagiarism;

@Repository
public interface CodePlagiarismRepository extends JpaRepository<Code_Plagiarism, Long> {

    List<Code_Plagiarism> findByScoreScoreId(Long scoreId);
}
