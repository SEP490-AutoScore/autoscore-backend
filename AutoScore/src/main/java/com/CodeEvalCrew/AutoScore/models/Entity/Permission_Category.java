package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Permission_Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long permission_category_id;

    private String permission_category_name;

    private boolean status;
    //Relationship
    //1-n permistion
    @OneToMany(mappedBy = "permission_category", cascade= CascadeType.ALL)
    private Set<Permission> permisions;
}
