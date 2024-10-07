package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campusId;

    private String campusName;

    private boolean status;

    //Relationship
    //1-n exam
    @OneToMany(mappedBy = "campus", cascade= CascadeType.ALL)
    private Set<Exam> exams;

    //1-n score
    @OneToMany(mappedBy = "campus", cascade= CascadeType.ALL)
    private Set<Score> scores;

    //1-n account
    @OneToMany(mappedBy = "campus", cascade = CascadeType.ALL)
    private Set<Account> accounts;
}
