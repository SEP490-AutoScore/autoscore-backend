package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long semesterId;
    private String semesterCode;
    private String semesterName;
    private boolean status;

    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Exam> exams;
}
