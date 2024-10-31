package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Column;
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
public class Postman_For_Grading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postmanForGradingId;

    @Column(nullable = false)
    private String postmanFunctionName;

    private Float scoreOfFunction;

    private Long totalPmTest;

    private Long orderBy;

    private Long postmanForGradingParentId;

    @ManyToOne
    @JoinColumn(name = "examQuestionId", nullable = false)
    private Exam_Question examQuestion;
}
