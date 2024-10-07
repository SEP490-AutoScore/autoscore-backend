package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
public class Score_Detail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scoreDetailId;

    private float baremScore;

    private String feedback;

    private float reReviewScore;

    private String reReviewFeedback;

    private LocalDateTime gradedAt;

    private LocalDateTime reReviewAt;

    private Long reReviewBy;

    private boolean isPass;

    //Relationship
    //1-1 account
    @OneToOne
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;

    //n-1 score
    @ManyToOne
    @JoinColumn(name = "scoreId", nullable = false)
    private Score score;

    //n-1 exam question
    @ManyToOne
    @JoinColumn(name = "examQuestionId", nullable = false)
    private Exam_Question examQuestion;

    //n-1 exam barem
    @ManyToOne
    @JoinColumn(name = "examBaremId", nullable = false)
    private Exam_Barem examBarem;

}
