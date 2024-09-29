package com.CodeEvalCrew.AutoScore.models.Entity;

import java.sql.Timestamp;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long score_id;

    private float total_score;

    private float re_review_total_score;

    private Timestamp graded_at;

    private Timestamp updated_at;

    private boolean status;

    private boolean flag;

    //Relationship
    //1-1 account
    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    //n-1 exam
    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    //n-1 exam paper
    @ManyToOne
    @JoinColumn(name = "exam_paper_id", nullable = false)
    private Exam_Paper exam_paper;

    //n-1 campus
    @ManyToOne
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    //n-1 student
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    //1-n score detail
    // @OneToMany
    // @JoinColumn(name = "score_detail_id", nullable = false)
    // private Set<Score_Detail> score_details;

    @OneToMany(mappedBy = "score", cascade = CascadeType.ALL)
    private Set<Score_Detail> score_details;
}