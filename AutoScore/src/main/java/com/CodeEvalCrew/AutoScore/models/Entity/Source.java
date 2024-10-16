package com.CodeEvalCrew.AutoScore.models.Entity;

import java.sql.Timestamp;
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
public class Source {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sourceId;

    private String originSourcePath;

    private Timestamp importTime;

    //Relationship
    //1-n source detail
    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL)
    private Set<Source_Detail> sourceDetails;

    @OneToOne
    @JoinColumn(name = "examPaperId", nullable = false)
    private Exam_Paper examPaper;

    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL)
    private Set<Student_Error> studentErrors;
}
