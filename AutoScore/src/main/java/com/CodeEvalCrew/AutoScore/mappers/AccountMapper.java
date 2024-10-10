package com.CodeEvalCrew.AutoScore.mappers;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CreateAccountRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AccountResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SignInWithGoogleResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Mapper
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Mapping(source = "accountId", target = "accountId")
    @Mapping(source = "email", target = "email")
    @Mapping(expression = "java(getRoleName(account))", target = "roleName")
    @Mapping(expression = "java(getPermissions(account))", target = "permissions")
    SignInWithGoogleResponseDTO accountToSignInWithGoogleResponseDTO(Account account);

    @Mapping(expression= "java(util.getAccountName(account.getCreatedBy()))", target = "createdBy")
    @Mapping(expression= "java(util.getAccountName(account.getUpdatedBy()))", target = "updatedBy")
    @Mapping(expression= "java(util.getAccountName(account.getDeletedBy()))", target = "deletedBy")
    AccountResponseDTO accountToAccountResponseDTO(Account account, @Context Util util);

    default String getRoleName(Account account) {
        return account.getAccountRoles() != null ? account.getAccountRoles().stream()
            .filter(Account_Role::isStatus)
            .map(role -> role.getRole().getRoleName())
            .findFirst()
            .orElse("Unknown") : "Unknown";
    }

    default Set<String> getPermissions(Account account) {
        return account.getAccountRoles() != null ? account.getAccountRoles().stream()
            .filter(Account_Role::isStatus)
            .flatMap(accountRole -> accountRole.getRole().getRole_permissions().stream())
            .filter(Role_Permission::isStatus)
            .map(rolePermission -> rolePermission.getPermission().getAction())
            .collect(Collectors.toSet()) : Set.of();
    }

    // default String getCampusName(Account account) {
    //     return account.getCampus() != null ? account.getCampus().getCampusName() : "Unknown";
    // }
}
