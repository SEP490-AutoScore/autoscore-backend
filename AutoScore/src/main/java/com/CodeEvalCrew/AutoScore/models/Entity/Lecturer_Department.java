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
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Lecturer_Department {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long lecturer_department_id;

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
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    //n-1 department
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    //1-1 aipromt
    @OneToOne
    @JoinColumn(name = "ai_prompt_id", nullable = false)
    private AI_Prompt ai_prompt;

    //1-1 acocunt
    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
