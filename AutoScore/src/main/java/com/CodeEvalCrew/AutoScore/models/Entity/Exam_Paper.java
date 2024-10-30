package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    private boolean status;
    private String instruction;
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
    private Boolean isComfirmFile;
    private Boolean isUsed;

    //Relationship
    //1-n score
    @OneToMany(mappedBy = "examPaper", cascade= CascadeType.ALL)
    private Set<Score> scores;

    //n-1 exam
    @ManyToOne
    @JoinColumn(name = "examId", nullable = false)
    private Exam exam;

    //1-n examquestion
    @OneToMany(mappedBy = "examPaper", cascade= CascadeType.ALL)
    private Set<Exam_Question> examQuestions;

    @OneToMany(mappedBy = "examPaper", cascade= CascadeType.ALL)
    private Set<Important_Exam_Paper> importants;
}
