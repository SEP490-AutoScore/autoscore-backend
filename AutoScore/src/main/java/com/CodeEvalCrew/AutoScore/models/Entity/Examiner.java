package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
public class Examiner {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examinerId;

    private boolean status;

    //Relationship
    //n-1 account
    @OneToOne
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;
}
