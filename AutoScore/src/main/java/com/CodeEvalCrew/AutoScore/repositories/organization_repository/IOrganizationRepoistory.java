package com.CodeEvalCrew.AutoScore.repositories.organization_repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Organization;

@Repository
public interface IOrganizationRepoistory extends JpaRepository<Organization, Long> {
    List<Organization> findByParentIdAndStatusTrue(Long parentId);
    Optional<Organization> findByParentIdIsNullAndStatusTrue();
    Optional<Organization> findByName(String name);
}
