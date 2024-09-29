package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Examiner {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long examiner_id;

    private boolean status;

    //Relationship
    //n-1 account
    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    //n-1 campus
    @ManyToOne
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

}
