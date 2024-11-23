package com.CodeEvalCrew.AutoScore.repositories.account_selected_key_repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Account_Selected_Key;

@Repository
public interface AccountSelectedKeyRepository extends JpaRepository<Account_Selected_Key, Long> {

    Optional<Account_Selected_Key> findByAccount_AccountId(Long accountId);
}
