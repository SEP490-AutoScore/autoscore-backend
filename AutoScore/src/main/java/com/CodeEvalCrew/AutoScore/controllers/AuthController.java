package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SingInWithEmailRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SignInWithGoogleResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.TokenResponseDTO;
import com.CodeEvalCrew.AutoScore.services.authentication.ISingInWithGoogleService;
import com.CodeEvalCrew.AutoScore.services.authentication.VerificationResponse;
import com.CodeEvalCrew.AutoScore.services.authentication.VerificationService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ISingInWithGoogleService singInWithGoogleService;
    private final VerificationService verificationService;

    public AuthController(ISingInWithGoogleService singInWithGoogleService, VerificationService verificationService) {
        this.singInWithGoogleService = singInWithGoogleService;
        this.verificationService = verificationService;
    }

    @PostMapping("/signingGoogle")
    public ResponseEntity<?> signInWithGoogle(@RequestBody String email) {
        try {
            if (email == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email not found");
            }
            SignInWithGoogleResponseDTO responseDTO = singInWithGoogleService.authenticateWithGoogle(email);
            if (responseDTO == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
            }
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @PostMapping("/signingEmail")
    public ResponseEntity<?> signInWithEmail(@RequestBody SingInWithEmailRequestDTO signin) {
        try {
            if (signin.getEmail() == null || signin.getPassword() == null || signin.getEmail().trim().isEmpty() || signin.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email not found");
            }
            SignInWithGoogleResponseDTO responseDTO = singInWithGoogleService.authenticateWithEmail(signin.getEmail(), signin.getPassword());
            if (responseDTO == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
            }
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponseDTO> refreshToken(@RequestBody String refreshToken) {
        return verificationService.rotationToken(refreshToken);
    }

    @PostMapping("/verify")
    public ResponseEntity<VerificationResponse> verifyToken(@RequestHeader(value = "Authorization", required = false) String token) {
        // Loại bỏ "Bearer " nếu token có định dạng "Bearer <token>"
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        VerificationResponse response = verificationService.verifyToken(token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
