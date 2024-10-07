package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
    private Long rolePermissionId;

    private boolean status;

    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    // Many-to-One relationship with Account
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "roleId", nullable = false)
    @ToString.Exclude
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id", referencedColumnName = "permissionId", nullable = false)
    @ToString.Exclude
    private Permission permission;
}
