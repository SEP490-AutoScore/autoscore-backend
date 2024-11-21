package com.CodeEvalCrew.AutoScore.security;

import java.security.Key;
import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration}")
    private long jwtExpiration;

    private Key key;

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
    public boolean validateToken(String token) throws ExpiredJwtException, MalformedJwtException, IllegalArgumentException, JwtException  {
    
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            logger.info("JWT token is valid");
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: " + e.getMessage());
            return false;
        } catch (JwtException e) {
            logger.error("Unexpected error during JWT validation: " + e.getMessage());
            return false;
        }
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
