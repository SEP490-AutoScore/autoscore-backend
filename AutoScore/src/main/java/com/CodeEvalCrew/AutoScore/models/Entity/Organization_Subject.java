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
public class Organization_Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long organizationSubjectId;

    private boolean status;
    //rlsp
    //n-1sub
    @ManyToOne
    @JoinColumn(name = "subjectId", nullable = false)
    private Subject subject;

    //n-1org
    @ManyToOne
    @JoinColumn(name = "organizationId", nullable = false)
    private Organization organization;
}
