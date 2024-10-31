package com.CodeEvalCrew.AutoScore.repositories.ai_info_repository;

import com.CodeEvalCrew.AutoScore.models.Entity.AI_Info;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AIInfoRepository extends JpaRepository<AI_Info, Long> {
    
}
