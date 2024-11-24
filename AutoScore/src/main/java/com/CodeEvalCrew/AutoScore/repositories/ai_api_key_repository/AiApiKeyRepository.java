package com.CodeEvalCrew.AutoScore.repositories.ai_api_key_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.AI_Api_Key;

@Repository
public interface AiApiKeyRepository extends JpaRepository<AI_Api_Key, Long> {
    // List<AI_Api_Key> findByAccountAccountIdInAndStatusTrue(List<Long> accountIds);

    List<AI_Api_Key> findByAccountAccountIdAndStatusTrue(Long createdBy);

    List<AI_Api_Key> findByStatusTrueAndIsSharedTrue();


}
