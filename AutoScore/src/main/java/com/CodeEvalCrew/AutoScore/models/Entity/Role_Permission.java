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
public class Role_Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long role_peemissionId;

    private boolean status;

    // Many-to-One relationship with Account
    @ManyToOne
    @JoinColumn(name = "permissionId", nullable = false)
    private Permission permission;

    // Many-to-One relationship with Role
    @ManyToOne
    @JoinColumn(name = "roleId", nullable = false)
    private Role role;
}
