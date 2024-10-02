package com.CodeEvalCrew.AutoScore.security;

import java.security.Key;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Tạo JWT token
    public String generateToken(String email, Set<String> roles, Set<String> permissions) {
       try {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
       } catch (Exception e) {
            throw new IllegalStateException("Cannot create token: " + e.getMessage());
       }
    }

    // Xác minh JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            // Log lỗi nếu cần
        } catch (ExpiredJwtException e) {
            // Token đã hết hạn
        } catch (UnsupportedJwtException e) {
            // Token không được hỗ trợ
        } catch (IllegalArgumentException e) {
            // Token rỗng hoặc chỉ chứa khoảng trắng
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
}