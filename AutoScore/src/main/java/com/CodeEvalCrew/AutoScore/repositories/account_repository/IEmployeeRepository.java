package com.CodeEvalCrew.AutoScore.repositories.account_repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Employee;
import com.CodeEvalCrew.AutoScore.models.Entity.Position;

@Repository
public interface IEmployeeRepository extends JpaRepository<Employee, Long>{
    Employee findByAccount_AccountId(Long accountId);
    List<Employee> findAllByOrganization_OrganizationId(Long organizationId);
    boolean existsByOrganizationOrganizationId(Long organizationId);
    Optional<List<Employee>> findAllByPosition(Position position);
}
