package com.CodeEvalCrew.AutoScore.repositories.role_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;

@Repository
public interface IRolePermissionRepository extends JpaRepository<Role_Permission, Long> {
    List<Role_Permission> findAllByRole_RoleId(Long roleId);
    List<Role_Permission> findAllByPermission_PermissionId(Long permissionId);
}
