package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long studentId;

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

    //1-1 campus
    @OneToOne
    @JoinColumn(name = "campusId", nullable = false)
    private Campus campus;
}
