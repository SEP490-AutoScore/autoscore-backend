package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Account_Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long account_role_id;

    private boolean status;

    // Many-to-One relationship with Account
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Many-to-One relationship with Role
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
