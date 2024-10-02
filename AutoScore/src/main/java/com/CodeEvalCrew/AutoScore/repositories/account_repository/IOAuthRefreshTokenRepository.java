package com.CodeEvalCrew.AutoScore.repositories.account_repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.OAuthRefreshToken;

public interface IOAuthRefreshTokenRepository extends JpaRepository<OAuthRefreshToken, Long>{
    // Tìm refresh token bằng giá trị token
    Optional<OAuthRefreshToken> findByToken(String token);

    // Xóa tất cả các refresh token cho một tài khoản
    void deleteByAccount(Account account);
}
