package com.CodeEvalCrew.AutoScore.services.account_service;

import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.CodeEvalCrew.AutoScore.models.Entity.Organization;

public class UserDetailsImpl extends User {
    private final Long accountId;
    private final Set<Organization> organizations; // Thêm tổ chức vào UserDetails

    public UserDetailsImpl(Long accountId, String email, Set<GrantedAuthority> authorities, Set<Organization> organizations) {
        super(email, "", authorities);
        this.accountId = accountId;
        this.organizations = organizations;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Set<Organization> getOrganizations() {
        return organizations;
    }
}
