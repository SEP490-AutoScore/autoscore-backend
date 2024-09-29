package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long permission_id;

    private String permission_name;

    private String action;

    // Relationship
    //n-1 category
    //n-1 campus
    @ManyToOne
    @JoinColumn(name = "permission_category_id", nullable = false)
    private Permission_Category permission_category;

    //n-n role
    @OneToMany(mappedBy = "permission")
    private Set<Role_Permission> role_permissions;
}
