package com.CodeEvalCrew.AutoScore.repositories.account_repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.CodeEvalCrew.AutoScore.models.Entity.AccountEntity;

public interface IAccountRepository extends JpaRepository<AccountEntity, Long>{
    
}
