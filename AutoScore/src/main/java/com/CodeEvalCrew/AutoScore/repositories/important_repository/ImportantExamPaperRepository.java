package com.CodeEvalCrew.AutoScore.repositories.important_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Important_Exam_Paper;

@Repository
public interface  ImportantExamPaperRepository extends JpaRepository<Important_Exam_Paper, Long> {
    List<Long> findImportantExamPaperIdByExamPaper_ExamPaperId(Long examPaperId);
}
