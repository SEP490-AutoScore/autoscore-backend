package com.CodeEvalCrew.AutoScore.repositories.content_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Content;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Purpose_Enum;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    // Find Content by AI API Key and Purpose, ordered by Order Priority
    List<Content> findByPurposeOrderByOrderPriority(Purpose_Enum purpose);

    List<Content> findAllByOrderByPurposeAscOrderPriorityAsc();
}
