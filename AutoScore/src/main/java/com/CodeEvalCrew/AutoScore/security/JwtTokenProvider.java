package com.CodeEvalCrew.AutoScore.security;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.RevokedToken;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.RevokedTokenRepository;

import io.jsonwebtoken.security.InvalidKeyException;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private Key key;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository; // Thêm Repository để tương tác với CSDL

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Tạo JWT token
    public String generateToken(String email, String role, Set<String> permissions) {
       try {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", "ROLE_" + role);
        claims.put("permissions", permissions);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
       } catch (InvalidKeyException e) {
            throw new IllegalStateException("Cannot create token: " + e.getMessage());
       }
    }

    // Xác minh JWT token và kiểm tra token có bị thu hồi không
    public boolean validateToken(String token) {
        try {
            if (isTokenRevoked(token)) {
                logger.warn("JWT token has been revoked");
                return false;
            }
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            logger.info("JWT token is valid");
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            logger.error("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }

    // Lấy email từ JWT token
    public String getEmailFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
        return claims.getSubject();
    }

    // Kiểm tra token có bị thu hồi hay không
    private boolean isTokenRevoked(String token) {
        Optional<RevokedToken> revokedToken = revokedTokenRepository.findByToken(token);
        return revokedToken.isPresent();
    }

    // Thu hồi token
    public void revokeToken(String token, Account account) {
        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setAccount(account);
        revokedToken.setToken(token);
        revokedToken.setRevokedAt(LocalDateTime.now());
        revokedTokenRepository.save(revokedToken);
        logger.info("Token has been revoked: " + token);
    }    
}
