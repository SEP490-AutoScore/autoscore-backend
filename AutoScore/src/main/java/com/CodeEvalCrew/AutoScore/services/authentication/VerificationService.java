package com.CodeEvalCrew.AutoScore.services.authentication;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.TokenResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.OAuthRefreshToken;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IOAuthRefreshTokenRepository;
import com.CodeEvalCrew.AutoScore.security.JwtTokenProvider;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

@Service
public class VerificationService {

    private final JwtTokenProvider jwtTokenProvider;
    private final IOAuthRefreshTokenRepository refreshTokenRepository;
    @Value("${jwt.refresh-token.expiration}")
    public long getJwtRefreshExpiration;

    public VerificationService(JwtTokenProvider jwtTokenProvider, IOAuthRefreshTokenRepository refreshTokenRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public VerificationResponse verifyToken(String token) {
        try {
            if (jwtTokenProvider.validateToken(token)) {
                return new VerificationResponse(HttpStatus.OK, "Token is valid.");
            } else {
                return new VerificationResponse(HttpStatus.FORBIDDEN, "Token has been revoked or invalid.");
            }
        } catch (ExpiredJwtException e) {
            return new VerificationResponse(HttpStatus.UNAUTHORIZED, "Token is expired. Details: " + e.getClaims());
        } catch (MalformedJwtException e) {
            return new VerificationResponse(HttpStatus.BAD_REQUEST, "Malformed token: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            return new VerificationResponse(HttpStatus.BAD_REQUEST, "Unsupported token format.");
        } catch (IllegalArgumentException e) {
            return new VerificationResponse(HttpStatus.BAD_REQUEST, "Token claims string is empty or null.");
        } catch (JwtException e) {
            return new VerificationResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during token validation: " + e.getMessage());
        }
    }

    public ResponseEntity<TokenResponseDTO> rotationToken(String refreshToken, String oldAccessToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .filter(oauthRefreshToken -> oauthRefreshToken.getExpiryDate().after(new Timestamp(System.currentTimeMillis())))
                .map(oauthRefreshToken -> {
                    Account account = oauthRefreshToken.getAccount();
                    if (account == null || account.getAccountId() == null) {
                        throw new IllegalStateException("Account or account_id cannot be null");
                    }

                    // Thu hồi access token cũ (nếu có)
                    if (oldAccessToken != null) {
                        jwtTokenProvider.revokeToken(oldAccessToken, account);
                    }

                    // Thu hồi refresh token cũ
                    refreshTokenRepository.delete(oauthRefreshToken);

                    Role role = account.getRole();
                    if (role == null || role.getRoleName() == null) {
                        throw new IllegalStateException("Account does not have a valid active role");
                    }

                    // Tạo danh sách quyền
                    Set<String> permissions = role.getRole_permissions().stream()
                            .filter(Role_Permission::isStatus)
                            .map(rolePermission -> rolePermission.getPermission().getAction())
                            .collect(Collectors.toSet());

                    // Tạo access token mới
                    String newAccessToken = jwtTokenProvider.generateToken(
                            account.getEmail(),
                            role.getRoleName(),
                            permissions
                    );

                    // Tạo refresh token mới
                    OAuthRefreshToken newRefreshToken = new OAuthRefreshToken();
                    newRefreshToken.setToken(UUID.randomUUID().toString());
                    newRefreshToken.setAccount(account);
                    newRefreshToken.setExpiryDate(Timestamp.from(Instant.now().plusMillis(getJwtRefreshExpiration)));
                    refreshTokenRepository.save(newRefreshToken);

                    // Trả về access token và refresh token mới
                    return ResponseEntity.ok(new TokenResponseDTO(newAccessToken, newRefreshToken.getToken(), getJwtRefreshExpiration));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null));
    }
}
