package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
public class Postman_For_Grading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postmanForGradingId;

    @Column(nullable = false)
    private String postmanFunctionName;

    private Float scoreOfFunction;

    private Long totalPmTest;

    private boolean status;
    
    private Long orderBy;

    private Long postmanForGradingParentId;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @Basic(fetch = FetchType.EAGER) // Buộc tải ngay lập tức
    private byte[] fileCollectionPostman;

    @ManyToOne
    @JoinColumn(name = "examQuestionId", nullable = true)
    private Exam_Question examQuestion;

   
    @OneToOne
    @JoinColumn(name = "gherkinScenarioId", referencedColumnName = "gherkinScenarioId", nullable = true)
    private Gherkin_Scenario gherkinScenario;

    @ManyToOne
    @JoinColumn(name = "examPaperId", nullable = false)
    private Exam_Paper examPaper;
}
