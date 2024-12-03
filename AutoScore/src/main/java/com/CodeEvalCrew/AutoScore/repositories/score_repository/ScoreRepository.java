package com.CodeEvalCrew.AutoScore.repositories.score_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentScoreDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Score;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    @Query("SELECT s FROM Student s WHERE s.studentId = :studentId")
    Student findStudentById(@Param("studentId") Long studentId);

    List<Score> findByExamPaperExamPaperId(Long examPaperId);

    @Query("SELECT DISTINCT s.examPaper.examPaperId FROM Score s")
    List<Long> findDistinctExamPaperIds();

    int countByExamPaperExamPaperId(Long examPaperId);

    int countByExamPaperExamPaperIdAndTotalScore(Long examPaperId, int totalScore);

    @Query("SELECT COUNT(s) FROM Score s WHERE s.examPaper.examPaperId = :examPaperId AND s.totalScore > :totalScore")
    int countByExamPaperIdAndTotalScoreGreaterThan(@Param("examPaperId") Long examPaperId, @Param("totalScore") int totalScore);
    

    @Query("SELECT s.student.studentCode, s.totalScore FROM Score s WHERE s.examPaper.examPaperId = :examPaperId")
    List<StudentScoreDTO> findStudentScoresByExamPaperId(@Param("examPaperId") Long examPaperId);

}
