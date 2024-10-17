package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;

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
public class Instructions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long instructionId;
    private String instructionName;
    private String introduction;
    private String important;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    @ManyToOne
    @JoinColumn(name = "subjectId", nullable = false)
    private Subject subject;
}
