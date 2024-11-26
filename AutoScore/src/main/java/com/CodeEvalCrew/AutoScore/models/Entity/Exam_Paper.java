package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Status_Enum;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
public class Exam_Paper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examPaperId;
    private String examPaperCode;
    @Enumerated(EnumType.STRING)
    private Exam_Status_Enum status;
    private String instruction;
    @Column(columnDefinition = "int default 90")
    private int duration = 90;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @Basic(fetch = FetchType.EAGER) // Buộc tải ngay lập tức
    private byte[] fileCollectionPostman;
    private Boolean isComfirmFile = false;
    private Boolean isUsed = false;

    //Relationship
    //1-n score
    @OneToMany(mappedBy = "examPaper", cascade= CascadeType.ALL)
    private Set<Score> scores;

    //n-1 exam
    @ManyToOne
    @JoinColumn(name = "examId", nullable = true)
    private Exam exam;

    //1-n examquestion
    @OneToMany(mappedBy = "examPaper", cascade= CascadeType.ALL)
    private Set<Exam_Question> examQuestions;

    @OneToMany(mappedBy = "examPaper", cascade= CascadeType.ALL)
    private Set<Important_Exam_Paper> importants;

    @OneToMany(mappedBy = "examPaper", cascade = CascadeType.ALL)
    private Set<Postman_For_Grading> postmanForGradings;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subjectId", referencedColumnName = "subjectId", nullable = false)
    private Subject subject;

}
