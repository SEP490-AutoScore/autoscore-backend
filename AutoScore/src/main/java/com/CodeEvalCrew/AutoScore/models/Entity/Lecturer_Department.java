package com.CodeEvalCrew.AutoScore.models.Entity;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Lecturer_Department {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long lecturer_departmentId;

    private boolean isHeader;

    @NotNull
    @Past // Thời điểm tạo phải là trong quá khứ
    private Timestamp createdAt;

    private long createdBy;

    private Timestamp updatedAt;

    private long updatedBy;

    private Timestamp deletedAt;

    private long deletedBy;

    //Relationship
    //n-1 lectuer
    @ManyToOne
    @JoinColumn(name = "lecturerId", nullable = false)
    private Lecturer lecturer;

    //n-1 department
    @ManyToOne
    @JoinColumn(name = "departmentId", nullable = false)
    private Department department;

    //1-1 aipromt
    @OneToOne
    @JoinColumn(name = "ai_promptId", nullable = false)
    private AI_Prompt aiPrompt;

    //1-1 acocunt
    @OneToOne
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;
}
