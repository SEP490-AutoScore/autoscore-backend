package com.CodeEvalCrew.AutoScore.repositories.grading_process_repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.GradingStatusEnum;
import com.CodeEvalCrew.AutoScore.models.Entity.GradingProcess;

public interface GradingProcessRepository
        extends JpaRepository<GradingProcess, Long>, JpaSpecificationExecutor<GradingProcess> {
    // Method to find if any GradingProcess has a status in the specified list
    boolean existsByStatusIn(List<GradingStatusEnum> statuses);

    // Method to find all GradingProcess with PENDING status
    List<GradingProcess> findByStatus(GradingStatusEnum status);

    Optional<GradingProcess> findByExamPaper_ExamPaperId(Long examPaperId);
}
