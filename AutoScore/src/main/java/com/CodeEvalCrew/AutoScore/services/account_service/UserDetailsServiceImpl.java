package com.CodeEvalCrew.AutoScore.services.account_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;

import jakarta.transaction.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IAccountRepository accountRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Set<GrantedAuthority> authorities = account.getAccountRoles().stream()
                .filter(accountRole -> accountRole.isStatus() && accountRole.getRole() != null) // Filter active roles with non-null Role
                // Add roles with "ROLE_" prefix
                .map(accountRole -> new SimpleGrantedAuthority("ROLE_" + accountRole.getRole().getRoleName()))
                .collect(Collectors.toSet());  // Collect roles first

        // Now, add permissions
        authorities.addAll(
                account.getAccountRoles().stream()
                        .flatMap(accountRole -> accountRole.getRole().getRole_permissions().stream()) // Get permissions from roles
                        .filter(rolePermission -> rolePermission.isStatus() && rolePermission.getPermission() != null) // Filter active permissions
                        .map(rolePermission -> new SimpleGrantedAuthority(rolePermission.getPermission().getAction()))
                        .collect(Collectors.toSet()) // Collect permissions
        );

        return new UserDetailsImpl(account.getAccountId(), account.getEmail(), authorities);
    }
}
