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
public class Source_Detail {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long sourceDetailId;

    private String studentSourceCodePath;
    //Relationship
    //n-1 student
    @ManyToOne
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    //n-1 source
    @ManyToOne
    @JoinColumn(name = "sourceId", nullable = false)
    private Source source;
}
