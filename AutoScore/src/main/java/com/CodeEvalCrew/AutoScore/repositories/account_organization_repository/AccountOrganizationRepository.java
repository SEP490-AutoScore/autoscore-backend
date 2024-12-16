package com.CodeEvalCrew.AutoScore.repositories.account_organization_repository;


import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Account_Organization;

@Repository
public interface AccountOrganizationRepository extends JpaRepository<Account_Organization, Long> {
    Optional<Account_Organization> findByAccountAccountIdAndStatusTrue(Long accountId);
    List<Account_Organization> findByOrganizationOrganizationIdInAndStatusTrue(Set<Long> organizationIds);
    boolean existsByOrganizationOrganizationId(Long organizationId);

    List<Account_Organization> findByAccount_AccountId(Long accountId);
    List<Account_Organization> findByOrganizationOrganizationIdAndStatusTrue(Long organizationId);

}