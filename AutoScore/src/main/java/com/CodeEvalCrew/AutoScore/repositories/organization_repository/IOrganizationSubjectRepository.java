package com.CodeEvalCrew.AutoScore.repositories.organization_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Organization_Subject;

@Repository
public interface IOrganizationSubjectRepository extends JpaRepository<Organization_Subject, Long> {
    boolean existsByOrganizationOrganizationId(Long organizationId);
}
