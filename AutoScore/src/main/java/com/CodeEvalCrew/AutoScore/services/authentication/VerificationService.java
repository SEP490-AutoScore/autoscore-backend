package com.CodeEvalCrew.AutoScore.services.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.security.JwtTokenProvider;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

@Service
public class VerificationService {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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
}
