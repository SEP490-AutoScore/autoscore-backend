package com.CodeEvalCrew.AutoScore.models.Entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Score_Detail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long score_detail_id;

    private float barem_score;

    private String feedback;

    private float re_review_score;

    private String re_review_feedback;

    private Timestamp graded_at;

    private Timestamp re_review_at;

    private long re_review_by;

    private boolean isPass;

    //Relationship
    //1-1 account
    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    //n-1 score
    @ManyToOne
    @JoinColumn(name = "score_id", nullable = false)
    private Score score;

    //n-1 exam question
    @ManyToOne
    @JoinColumn(name = "exam_question_id", nullable = false)
    private Exam_Question exam_question;

    //n-1 exam barem
    @ManyToOne
    @JoinColumn(name = "exam_barem_id", nullable = false)
    private Exam_Barem exam_barem;

}
