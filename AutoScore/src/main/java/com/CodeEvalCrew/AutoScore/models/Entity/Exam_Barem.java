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
public class Exam_Barem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long exam_barem_id;

    private String barem_content;

    private float barem_max_score;

    private String detail;

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

    //n-1 exam question
    @ManyToOne
    @JoinColumn(name = "exam_question_id", nullable = false)
    private Exam_Question exam_question;

    //1-1 test case
    @OneToOne
    @JoinColumn(name = "test_case_id", nullable = true)
    private Test_Case test_case;

    //1-n score detail
    @OneToMany(mappedBy = "exam_barem", cascade= CascadeType.ALL)
    private Set<Score_Detail> score_details;
}
