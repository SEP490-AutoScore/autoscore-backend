package com.CodeEvalCrew.AutoScore.models.Entity;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Status_Enum;

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
public class Important_Exam_Paper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long importantExamPaperId;
    private Exam_Status_Enum status;
    @ManyToOne
    @JoinColumn(name = "importantId", nullable = false)
    private Important important;
    @ManyToOne
    @JoinColumn(name = "examPaperId", nullable = false)
    private Exam_Paper examPaper;
}
