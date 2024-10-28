package com.CodeEvalCrew.AutoScore.models.Entity;

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
public class Important {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long importantId;
    private String importantName;
    private String importantCode;
    @ManyToOne
    @JoinColumn(name = "subjectId", nullable = false)
    private Subject subject;
}
