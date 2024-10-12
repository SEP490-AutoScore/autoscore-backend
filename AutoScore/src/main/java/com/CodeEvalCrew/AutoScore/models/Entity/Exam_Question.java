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
public class Exam_Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examQuestionId;

    private String questionContent;

    private String questionNumber;

    private float maxScore;

    private String type;

    private boolean status;
    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    // Relationship

    // n-1 exam_paper
    @ManyToOne
    @JoinColumn(name = "examPaperId", nullable = false)
    private Exam_Paper examPaper;

    // 1-n barem
    @OneToMany(mappedBy = "examQuestion", cascade = CascadeType.ALL)
    private Set<Exam_Barem> examBarems;

    // 1-n score detail
    @OneToMany(mappedBy = "examQuestion", cascade = CascadeType.ALL)
    private Set<Score_Detail> scoreDetails;
}
