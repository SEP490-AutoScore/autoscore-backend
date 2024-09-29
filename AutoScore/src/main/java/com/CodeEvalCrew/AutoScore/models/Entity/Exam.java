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
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long exam_id;

    private String exam_code;

    private Timestamp exam_at;

    private Timestamp grading_at;

    private Timestamp publish_at;

    private String semester_name;

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
    //n-1 subject
    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    //1-1 account
    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    //n-1 campus
    @ManyToOne
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    //1-n - score
    @OneToMany(mappedBy = "exam", cascade= CascadeType.ALL)
    private Set<Score> scores;

    @OneToMany(mappedBy = "exam", cascade= CascadeType.ALL)
    private Set<Exam_Paper> exam_papers;    
}
