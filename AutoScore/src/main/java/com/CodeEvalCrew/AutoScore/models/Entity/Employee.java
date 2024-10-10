package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    private String fullName;

    private String employeeCode;

    private boolean status;

    //Relationship
    //n-1 org
    @ManyToOne
    @JoinColumn(name = "organizationId", nullable = false)
    private Organization organization;

    //n-1 posi
    @ManyToOne
    @JoinColumn(name = "positionId", nullable = false)
    private Position position;

    //1-1 prompt
    @OneToOne
    @JoinColumn(name = "aiPromptId", nullable = true)
    private AI_Prompt aiPrompt;

    //1-1 acc
    @OneToOne
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;
}
