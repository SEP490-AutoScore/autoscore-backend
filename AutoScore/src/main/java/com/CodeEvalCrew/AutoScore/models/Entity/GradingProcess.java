package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GradingProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long processId;
    private String status;
    private int successProcess;
    private int totalProcess;
    private LocalDateTime startDate;
    private LocalDateTime updateDate;
    //n-1 exam
    @OneToOne
    @JoinColumn(name = "examPaperId", nullable = true)
    private Exam_Paper examPaper;
}
