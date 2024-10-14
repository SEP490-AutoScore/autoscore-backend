package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scoreId;

    private float totalScore;

    private float reReviewTotalScore;

    private LocalDateTime gradedAt;

    private LocalDateTime updatedAt;

    private boolean status;

    private boolean flag;

    //Relationship
    //n-1 exam
    @ManyToOne
    @JoinColumn(name = "examId", nullable = false)
    private Exam exam;

    //n-1 exam paper
    @ManyToOne
    @JoinColumn(name = "examPaperId", nullable = false)
    private Exam_Paper examPaper;

    //n-1 student
    @ManyToOne
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "organizationId", nullable = false)
    private Organization organization;

    //1-n score detail
    // @OneToMany
    // @JoinColumn(name = "score_detailId", nullable = false)
    // private Set<Score_Detail> score_details;

    @OneToMany(mappedBy = "score", cascade = CascadeType.ALL)
    private Set<Score_Detail> scoreDetails;
}