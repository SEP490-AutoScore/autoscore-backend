package com.CodeEvalCrew.AutoScore.repositories.account_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Account;

@Repository
public interface IAccountRepository extends JpaRepository<Account, Long>{
    Account findByEmail(String email);
}
