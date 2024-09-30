package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long campus_id;

    private String campus_name;

    private boolean status;

    //Relationship
    //1-n lecturer
    @OneToMany(mappedBy = "campus", cascade= CascadeType.ALL)
    private Set<Lecturer> lecturers;

    //1-n examiner
    @OneToMany(mappedBy = "campus", cascade= CascadeType.ALL)
    private Set<Examiner> examiners;

    //1-n exam
    @OneToMany(mappedBy = "campus", cascade= CascadeType.ALL)
    private Set<Exam> exams;

    //1-n score
    @OneToMany(mappedBy = "campus", cascade= CascadeType.ALL)
    private Set<Score> scores;
}
