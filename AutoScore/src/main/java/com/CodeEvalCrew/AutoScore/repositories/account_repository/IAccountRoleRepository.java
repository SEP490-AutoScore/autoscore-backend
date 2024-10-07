package com.CodeEvalCrew.AutoScore.repositories.account_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Account_Role;

@Repository
public interface IAccountRoleRepository extends JpaRepository<Account_Role, Long> {
    List<Account_Role> findAllByAccount_AccountId(Long accountId);
    List<Account_Role> findAllByRole_RoleId(Long roleId);
}
