package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import jakarta.persistence.CascadeType;
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
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    private String studentCode;

    private String studentEmail;

    private boolean status;

    //Relationship
    //1-n score
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private Set<Score> scores;

    //1-n source_detail
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private Set<Source_Detail> sourceDetails;

    @ManyToOne
    @JoinColumn(name = "organizationId", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "examId", nullable = false)
    private Exam exam;
}
