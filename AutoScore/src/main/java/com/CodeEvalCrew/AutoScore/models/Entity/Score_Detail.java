package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private String postmanFunctionName;
    private Float scoreOfFunction;
    private Long totalPmtest;
    private Float scoreAchieve;
    private Long noPmtestAchieve;

    //Relationship
    //n-1 score
    @ManyToOne
    @JoinColumn(name = "scoreId", nullable = false)
    private Score score;

    //n-1 exam question
    @ManyToOne
    @JoinColumn(name = "examQuestionId", nullable = false)
    private Exam_Question examQuestion;
}
