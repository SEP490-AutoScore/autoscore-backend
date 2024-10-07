package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Permission_Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionCategoryId;

    private String permissionCategoryName;

    private boolean status;
    
    //Relationship
    //1-n permistion
    @OneToMany(mappedBy = "permissionCategory", cascade= CascadeType.ALL)
    @JsonBackReference
    private Set<Permission> permisions;
}
