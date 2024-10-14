package com.CodeEvalCrew.AutoScore.repositories.organization_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Organization;

@Repository
public interface IOrganizationRepoistory extends JpaRepository<Organization, Long> {
    
}
