package com.CodeEvalCrew.AutoScore.services.account_service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;

import jakarta.transaction.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IAccountRepository accountRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tìm account theo email
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Kiểm tra Role của Account
        Role role = account.getRole();
        if (role == null || !role.isStatus() || role.getRoleCode() == null) {
            throw new UsernameNotFoundException("Account has no valid role assigned.");
        }

        // Tạo danh sách GrantedAuthority (quyền) cho UserDetails
        Set<GrantedAuthority> authorities = role.getRole_permissions().stream()
                .filter(Role_Permission::isStatus)
                .map(rolePermission -> new SimpleGrantedAuthority(rolePermission.getPermission().getAction()))
                .collect(Collectors.toSet());

        // Thêm quyền hạn của Role (ROLE_)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));

        // Lấy danh sách các tổ chức mà tài khoản liên kết
        Set<Organization> organizations = account.getAccountOrganizations().stream()
                .filter(Account_Organization::isStatus) // Lọc các Account_Organization có status là true
                .map(Account_Organization::getOrganization)
                .collect(Collectors.toSet());

        // Trả về đối tượng UserDetailsImpl với danh sách quyền hạn và tổ chức
        return new UserDetailsImpl(account.getAccountId(), account.getEmail(), authorities, organizations);
    }
}

