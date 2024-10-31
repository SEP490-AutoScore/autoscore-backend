package com.CodeEvalCrew.AutoScore.repositories.ai_info_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.AI_Info;

@Repository
public interface AIInfoRepository extends JpaRepository<AI_Info, Long> {
    List<AI_Info> findByPurpose(String purpose);
}
