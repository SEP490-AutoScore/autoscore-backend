package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
public class Gherkin_Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gherkinScenarioId;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String gherkinData;

    private String postmanFunctionName;

    private float baremDetailScore;

    private Long gherkinScenarioParentId;

    private int gherkinScenarioPosition;

    private Boolean statusParentId;

    // Quan hệ n-1 với Exam_Barem
    @ManyToOne
    @JoinColumn(name = "examBaremId", nullable = false)
    private Exam_Barem examBarem;
}
