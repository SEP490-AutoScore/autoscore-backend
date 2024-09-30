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
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long student_id;

    private String student_code;

    private String student_email;

    private boolean status;

    //Relationship
    //1-n score
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private Set<Score> scores;

    //1-n source_detail
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private Set<Source_Detail> source_details;

    //1-1 campus
    @OneToOne
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;
}
