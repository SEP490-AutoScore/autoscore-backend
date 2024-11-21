package com.CodeEvalCrew.AutoScore.mappers;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AccountResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SignInWithGoogleResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Mapper
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Mapping(source = "email", target = "email")
    @Mapping(expression = "java(getRoleName(account))", target = "role")
    @Mapping(expression = "java(getPermissions(account))", target = "permissions")
    SignInWithGoogleResponseDTO accountToSignInWithGoogleResponseDTO(Account account);

    @Mapping(expression= "java(util.getEmployeeFullName(account.getCreatedBy()))", target = "createdBy")
    @Mapping(expression= "java(util.getEmployeeFullName(account.getUpdatedBy()))", target = "updatedBy")
    @Mapping(expression= "java(util.getEmployeeFullName(account.getDeletedBy()))", target = "deletedBy")
    AccountResponseDTO accountToAccountResponseDTO(Account account, @Context Util util);

    default String getRoleName(Account account) {
        return account.getRole() != null ? account.getRole().getRoleCode() : "Unknown";
    }

    default Set<String> getPermissions(Account account) {
        return account.getRole() != null ? account.getRole().getRole_permissions().stream()
                .filter(rolePermission -> rolePermission.isStatus() && rolePermission.getPermission() != null)
                .map(rolePermission -> rolePermission.getPermission().getAction())
                .collect(Collectors.toSet()) : Set.of();
    }
}
