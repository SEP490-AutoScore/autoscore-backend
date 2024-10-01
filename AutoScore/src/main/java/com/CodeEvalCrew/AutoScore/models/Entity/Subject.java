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
public class Subject {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long subjectId;

    private String subjectName;

    private String subjectCode;

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

    //1-n exam
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private Set<Exam> exams;

    //1-1 department
    @OneToOne
    @JoinColumn(name = "departmentId", nullable = false)
    private Department department;
}
