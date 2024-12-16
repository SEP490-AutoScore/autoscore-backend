package com.CodeEvalCrew.AutoScore.repositories.ai_api_key_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.AI_Api_Key;

@Repository
public interface AiApiKeyRepository extends JpaRepository<AI_Api_Key, Long> {

    List<AI_Api_Key> findByAccountAccountIdAndStatusTrue(Long accountId);

    List<AI_Api_Key> findByStatusTrueAndSharedTrue();

    boolean existsByAiApiKeyAndAccount_AccountIdAndStatusTrue(String aiApiKey, Long accountId);

}
