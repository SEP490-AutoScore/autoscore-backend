package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student_Error {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentErrorId;
    @Lob
    private String errorContent;

    @ManyToOne
    @JoinColumn(name = "sourceId", nullable = false)
    private Source source;

    @OneToOne
    @JoinColumn(name = "studentId", nullable = true)
    private Student student;
}
