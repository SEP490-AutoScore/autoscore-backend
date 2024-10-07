package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionId;

    private String permissionName;

    private String action;

    private boolean status;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "permissionCategoryId", nullable = false)
    @JsonManagedReference
    private Permission_Category permissionCategory;

    //n-n role
    @OneToMany(mappedBy = "permission")
    private Set<Role_Permission> rolePermissions;
}
