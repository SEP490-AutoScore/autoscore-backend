package com.CodeEvalCrew.AutoScore.repositories.ai_api_key_repository;

// import java.util.List;
// import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.AI_Api_Key;


@Repository
public interface AiApiKeyRepository extends JpaRepository<AI_Api_Key, Long> {

    // // Find AI_Api_Key by ID
    // Optional<AI_Api_Key> findById(Long id);

    // // Find all AI_Api_Key with a specific purpose and status
    // List<AI_Api_Key> findByStatus(boolean status);
}
