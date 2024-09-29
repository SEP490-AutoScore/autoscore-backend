package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Department {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long department_id;

    private String department_name;
    
    private String dev_language;

    private boolean status;

    //Relationship
    //1-n lectuerdepartment
    @OneToMany(mappedBy = "department", cascade= CascadeType.ALL)
    private Set<Lecturer_Department> lecturerDepartments;

}
