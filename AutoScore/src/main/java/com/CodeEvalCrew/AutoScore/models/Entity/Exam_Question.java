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
public class Exam_Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long question_id;

    private String question_content;

    private String question_number;

    private float max_score;

    private String type;
    
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

    //n-1 exam_paper
    @ManyToOne
    @JoinColumn(name = "exam_paper_id", nullable = false)
    private Exam_Paper exam_paper;

    //1-n barem
    @OneToMany(mappedBy = "exam_question", cascade= CascadeType.ALL)
    private Set<Exam_Barem> exam_barems;

    //1-n score detail
    // @OneToMany
    // @JoinColumn(name = "score_detail_id", nullable = false)
    // private Set<Score_Detail> scores_details;
    @OneToMany(mappedBy = "exam_question", cascade= CascadeType.ALL)
    private Set<Score_Detail> score_details;
}
