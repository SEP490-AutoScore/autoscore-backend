package com.CodeEvalCrew.AutoScore.repositories.account_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Employee;

@Repository
public interface  IEmployeeRepository extends JpaRepository<Employee, Long>{
    Employee findByAccount_AccountId(Long accountId);
}