package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class Exam_Part {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examPartId;
    private String partName;
    private boolean status;
      private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;
    //rlsp
    //exampp
    @ManyToOne
    @JoinColumn(name = "examPaperId", nullable = false)
    private Exam_Paper examPaper;

    @OneToMany
    @JoinColumn(name = "examQuestionId", nullable = true)
    private Set<Exam_Question> examQuestions;
    
}
