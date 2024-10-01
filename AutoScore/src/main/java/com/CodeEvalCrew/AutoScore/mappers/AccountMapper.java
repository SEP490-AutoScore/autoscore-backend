package com.CodeEvalCrew.AutoScore.mappers;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SignInWithGoogleResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;

@Mapper
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Mapping(source = "accountId", target = "accountId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    @Mapping(expression = "java(getCampusName(account))", target = "campusName")
    @Mapping(expression = "java(getRoleName(account))", target = "roleName")
    @Mapping(expression = "java(getPermissions(account))", target = "permissions")
    SignInWithGoogleResponseDTO accountToSignInWithGoogleResponseDTO(Account account);

    default String getRoleName(Account account) {
        return account.getAccount_roles().stream()
            .filter(Account_Role::isStatus)
            .map(role -> role.getRole().getRoleName())
            .findFirst()
            .orElse("Unknown");
    }

    default Set<String> getPermissions(Account account) {
        return account.getAccount_roles().stream()
            .filter(Account_Role::isStatus)
            .flatMap(accountRole -> accountRole.getRole().getRole_permissions().stream())
            .filter(Role_Permission::isStatus)
            .map(rolePermission -> rolePermission.getPermission().getAction())
            .collect(Collectors.toSet());
    }

    default String getCampusName(Account account) {
        return account.getCampus().getCampusName();
    }
}
