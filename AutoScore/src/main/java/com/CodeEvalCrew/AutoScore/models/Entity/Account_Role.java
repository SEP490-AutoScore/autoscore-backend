package com.CodeEvalCrew.AutoScore.models.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Account_Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long accountRoleId;

    private boolean status;

    // Many-to-One relationship with Account
    @ManyToOne
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;

    // Many-to-One relationship with Role
    @ManyToOne
    @JoinColumn(name = "roleId", nullable = false)
    private Role role;
}
