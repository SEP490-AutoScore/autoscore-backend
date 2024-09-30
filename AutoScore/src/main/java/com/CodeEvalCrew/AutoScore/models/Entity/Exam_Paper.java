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
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Exam_Paper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long exam_paper_id;

    private String exam_paper_code;

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
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    //1-n score
    @OneToMany(mappedBy = "exam_paper", cascade= CascadeType.ALL)
    private Set<Score> scores;

    //n-1 exam
    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    //1-n examquestion
    @OneToMany(mappedBy = "exam_paper", cascade= CascadeType.ALL)
    private Set<Exam_Question> exam_questions;

    //1-1 exam db
    @OneToOne
    @JoinColumn(name = "exam_database_id", nullable = false)
    private Exam_Database exam_database;

    //1-1 source
    @OneToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;
}
