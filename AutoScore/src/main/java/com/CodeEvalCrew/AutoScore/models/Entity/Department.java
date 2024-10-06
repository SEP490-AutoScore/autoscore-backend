package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

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
public class Department {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentId;

    private String departmentName;
    
    private String devLanguage;

    private boolean status;

    //Relationship
    //1-n lectuerdepartment
    @OneToMany(mappedBy = "department", cascade= CascadeType.ALL)
    private Set<Lecturer_Department> lecturerDepartments;

}
