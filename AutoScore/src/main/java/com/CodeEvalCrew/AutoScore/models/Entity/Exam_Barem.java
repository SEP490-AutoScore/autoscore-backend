package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
public class Exam_Barem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examBaremId;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String baremContent;
    private float baremMaxScore;
    private String endpoint;
    private String allowRole;
    private String method;
    private String baremFunction;
    private String payloadType;
    @Column(columnDefinition = "TEXT")
    private String payload;
    private String validation;
    @Column(length = 65535)
    private String successResponse;
    @Column(columnDefinition = "TEXT")
    private String errorResponse;
    private boolean status;
    private int orderBy;

    //Relationship
    //n-1 exam question
    @ManyToOne
    @JoinColumn(name = "examQuestionId", nullable = false)
    private Exam_Question examQuestion;

    //1-1 test case
    @OneToMany
    @JoinColumn(name = "testCaseId", nullable = true)
    private Set<Test_Case> testCases;

    //1-n score detail
    @OneToMany(mappedBy = "examBarem", cascade= CascadeType.ALL)
    private Set<Score_Detail> scoreDetails;
}
