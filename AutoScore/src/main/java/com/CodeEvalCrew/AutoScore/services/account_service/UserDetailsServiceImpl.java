package com.CodeEvalCrew.AutoScore.services.account_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IAccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Set<GrantedAuthority> authorities = account.getAccountRoles().stream()
            .filter(Account_Role::isStatus) // Chỉ lấy các role đang hoạt động
            .flatMap(accountRole -> accountRole.getRole().getRole_permissions().stream())
            .filter(Role_Permission::isStatus) // Chỉ lấy các permission đang hoạt động
            .map(rolePermission -> new SimpleGrantedAuthority(rolePermission.getPermission().getAction()))
            .collect(Collectors.toSet());

        // Tạo đối tượng UserDetails
        return new org.springframework.security.core.userdetails.User(
            account.getEmail(),
            "N/A",
            authorities
        );
    }
}