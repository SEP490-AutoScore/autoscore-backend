package com.CodeEvalCrew.AutoScore.models.Entity;

import java.sql.Timestamp;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
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
    private long examPaperId;

    private String examPaperCode;

    private boolean status;

    @NotNull
    @Past // Thời điểm tạo phải là trong quá khứ
    private Timestamp createdAt;

    private long createdBy;

    private Timestamp updatedAt;

    private long updatedBy;

    private Timestamp deletedAt;

    private long deletedBy;

    //Relationship
    //1-1 account
    @OneToOne
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;

    //1-n score
    @OneToMany(mappedBy = "examPaper", cascade= CascadeType.ALL)
    private Set<Score> scores;

    //n-1 exam
    @ManyToOne
    @JoinColumn(name = "examId", nullable = false)
    private Exam exam;

    //1-n examquestion
    @OneToMany(mappedBy = "examPaper", cascade= CascadeType.ALL)
    private Set<Exam_Question> exam_questions;

    //1-1 exam db
    @OneToOne
    @JoinColumn(name = "examDatabaseId", nullable = false)
    private Exam_Database exam_database;

    //1-1 source
    @OneToOne
    @JoinColumn(name = "sourceId", nullable = false)
    private Source source;
}
