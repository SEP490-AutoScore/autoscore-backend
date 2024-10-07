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
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Past;
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

    private String baremContent;

    private float baremMaxScore;

    private String detail;

    private String type;

    private boolean status;

    @Past
    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    //Relationship
    //1-1 account
    @OneToOne
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;

    //n-1 exam question
    @ManyToOne
    @JoinColumn(name = "examQuestionId", nullable = false)
    private Exam_Question examQuestion;

    //1-1 test case
    @OneToOne
    @JoinColumn(name = "testCaseId", nullable = true)
    private Test_Case testCase;

    //1-n score detail
    @OneToMany(mappedBy = "examBarem", cascade= CascadeType.ALL)
    private Set<Score_Detail> scoreDetails;
}
