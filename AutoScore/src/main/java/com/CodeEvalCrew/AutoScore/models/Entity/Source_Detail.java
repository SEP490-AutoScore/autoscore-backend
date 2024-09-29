package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Source_Detail {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long source_detail_id;

    private String student_source_code_path;
    //Relationship
    //n-1 student
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    //n-1 source
    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;
}
