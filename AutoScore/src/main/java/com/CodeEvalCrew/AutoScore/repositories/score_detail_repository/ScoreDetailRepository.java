package com.CodeEvalCrew.AutoScore.repositories.score_detail_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Score_Detail;

@Repository
public interface ScoreDetailRepository extends JpaRepository<Score_Detail, Long> {
    List<Score_Detail> findByScore_ScoreId(Long scoreId);
    List<Score_Detail> findByExamQuestion_ExamQuestionId(Long examQuestionId);
}
