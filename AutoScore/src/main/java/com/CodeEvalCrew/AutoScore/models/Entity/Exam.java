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
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examId;

    private String examCode;

    private Timestamp examAt;

    private Timestamp gradingAt;

    private Timestamp publishAt;

    private String semesterName;

    private boolean status;

    @NotNull
    @Past // Thời điểm tạo phải là trong quá khứ
    private Timestamp createdAt;

    private Long createdBy;

    private Timestamp updatedAt;

    private Long updatedBy;

    private Timestamp deletedAt;

    private Long deletedBy;

    //Relationship
    //n-1 subject
    @ManyToOne
    @JoinColumn(name = "subjectId", nullable = false)
    private Subject subject;

    //1-1 account
    @OneToOne
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;

    //n-1 campus
    @ManyToOne
    @JoinColumn(name = "campusId", nullable = false)
    private Campus campus;

    //1-n - score
    @OneToMany(mappedBy = "exam", cascade= CascadeType.ALL)
    private Set<Score> scores;

    @OneToMany(mappedBy = "exam", cascade= CascadeType.ALL)
    private Set<Exam_Paper> exam_papers;    
}
