package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scoreId;

    private Float totalScore;
    private String reason;

    private LocalDateTime gradedAt;

    private String levelOfPlagiarism;
    @Lob
    private String plagiarismReason;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String logRunPostman;

    //Relationship
    //n-1 exam paper
    @ManyToOne
    @JoinColumn(name = "examPaperId", nullable = false)
    private Exam_Paper examPaper;

    //n-1 student
    @ManyToOne
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    // @ManyToOne
    // @JoinColumn(name = "organizationId", nullable = false)
    // private Organization organization;

    //1-n score detail
    @OneToMany(mappedBy = "score", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Score_Detail> scoreDetails;

    @OneToMany(mappedBy = "score", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Code_Plagiarism> codePlagiarisms;
}