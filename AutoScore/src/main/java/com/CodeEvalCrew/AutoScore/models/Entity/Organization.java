package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long organizationId;

    private Long parentId;

    private String name;

    @Enumerated(EnumType.STRING)
    private Organization_Enum type;

    private boolean status;
    //rels
    //1-n emp
    @OneToMany
    @JoinColumn(name = "employeeId", nullable = true)
    private Set<Employee> employees;

    //1-n suborg
    @OneToMany
    @JoinColumn(name = "subjectOrgId", nullable = true)
    private Set<Organization_Subject> organizationSubjects;

    @OneToMany(mappedBy = "organization", cascade= CascadeType.ALL)
    private Set<Account_Organization> accountOrganizations;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private Set<Student> students;
}
