package com.CodeEvalCrew.AutoScore.mappers;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SignInWithGoogleResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;

@Mapper
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "accountId", target = "id")
    @Mapping(expression = "java(getRoleName(account))", target = "role")
    @Mapping(expression = "java(getPermissions(account))", target = "permissions")
    SignInWithGoogleResponseDTO accountToSignInWithGoogleResponseDTO(Account account);

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
